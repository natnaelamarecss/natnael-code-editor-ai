package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.Project
import com.example.data.model.ProjectFile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

private const val TAG = "ProjectRepository"

class ProjectRepository(private val context: Context) {
    private val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    private val auth get() = Firebase.auth

    // Local in-memory / offline cache for fast responsiveness and offline editing
    private val localProjects = MutableStateFlow<List<Project>>(emptyList())
    private val localFiles = MutableStateFlow<Map<String, List<ProjectFile>>>(emptyMap()) // projectId -> files

    init {
        // Pre-populate with starter default projects so new users immediately have code to run
        createSampleProjectsIfEmpty()
    }

    private fun currentUserId(): String {
        return auth.currentUser?.uid ?: "local_guest"
    }

    private fun isCloudEnabled(): Boolean {
        return auth.currentUser != null
    }

    fun observeProjects(): Flow<List<Project>> {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            return db.collection("users").document(uid).collection("projects")
                .snapshots()
                .map { snapshot ->
                    val list = snapshot.toObjects(Project::class.java)
                    if (list.isNotEmpty()) {
                        localProjects.value = list
                        list
                    } else {
                        localProjects.value
                    }
                }
        }
        return localProjects.asStateFlow()
    }

    fun observeProjectFiles(projectId: String): Flow<List<ProjectFile>> {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            return db.collection("users").document(uid).collection("projects").document(projectId).collection("files")
                .snapshots()
                .map { snapshot ->
                    val files = snapshot.toObjects(ProjectFile::class.java)
                    val updatedMap = localFiles.value.toMutableMap()
                    updatedMap[projectId] = files
                    localFiles.value = updatedMap
                    files
                }
        }
        return flow {
            val files = localFiles.value[projectId] ?: emptyList()
            emit(files)
        }
    }

    suspend fun getProject(projectId: String): Project? = withContext(Dispatchers.IO) {
        val local = localProjects.value.find { it.id == projectId }
        if (local != null) return@withContext local

        val uid = auth.currentUser?.uid ?: return@withContext null
        try {
            val doc = db.collection("users").document(uid).collection("projects").document(projectId).get().await()
            doc.toObject(Project::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get project: ${e.message}")
            null
        }
    }

    suspend fun getProjectFiles(projectId: String): List<ProjectFile> = withContext(Dispatchers.IO) {
        val local = localFiles.value[projectId]
        if (!local.isNullOrEmpty()) return@withContext local

        val uid = auth.currentUser?.uid ?: return@withContext emptyList()
        try {
            val snapshot = db.collection("users").document(uid).collection("projects").document(projectId).collection("files").get().await()
            val files = snapshot.toObjects(ProjectFile::class.java)
            val updatedMap = localFiles.value.toMutableMap()
            updatedMap[projectId] = files
            localFiles.value = updatedMap
            files
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get files: ${e.message}")
            emptyList()
        }
    }

    suspend fun createProject(
        name: String,
        description: String = "",
        template: String = "html"
    ): Project = withContext(Dispatchers.IO) {
        val projectId = "proj_${UUID.randomUUID().toString().take(8)}"
        val uid = currentUserId()
        val language = when (template.lowercase()) {
            "html" -> "html"
            "react" -> "jsx"
            "python" -> "python"
            "node" -> "javascript"
            else -> "text"
        }

        val newProject = Project(
            id = projectId,
            userId = uid,
            name = name,
            description = description,
            template = template,
            language = language
        )

        // Generate template files
        val files = generateStarterFiles(projectId, uid, template)

        // Update local state
        localProjects.value = listOf(newProject) + localProjects.value
        val updatedMap = localFiles.value.toMutableMap()
        updatedMap[projectId] = files
        localFiles.value = updatedMap

        // Sync to cloud if authenticated
        if (isCloudEnabled()) {
            try {
                val projPayload = mapOf(
                    "id" to projectId,
                    "userId" to uid,
                    "name" to name,
                    "description" to description,
                    "template" to template,
                    "language" to language,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                db.collection("users").document(uid).collection("projects").document(projectId).set(projPayload).await()

                for (file in files) {
                    val filePayload = mapOf(
                        "id" to file.id,
                        "projectId" to projectId,
                        "userId" to uid,
                        "name" to file.name,
                        "path" to file.path,
                        "content" to file.content,
                        "language" to file.language,
                        "isFolder" to file.isFolder,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    db.collection("users").document(uid).collection("projects").document(projectId).collection("files").document(file.id).set(filePayload).await()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Cloud sync failed for createProject: ${e.message}")
            }
        }

        newProject
    }

    suspend fun saveFileContent(projectId: String, fileId: String, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        val files = localFiles.value[projectId]?.toMutableList() ?: mutableListOf()
        val index = files.indexOfFirst { it.id == fileId }
        if (index != -1) {
            val updatedFile = files[index].copy(content = content)
            files[index] = updatedFile
            val updatedMap = localFiles.value.toMutableMap()
            updatedMap[projectId] = files
            localFiles.value = updatedMap
        }

        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                val fileRef = db.collection("users").document(uid).collection("projects").document(projectId).collection("files").document(fileId)
                fileRef.update(
                    mapOf(
                        "content" to content,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } catch (e: Exception) {
                Log.w(TAG, "Cloud sync saveFileContent error: ${e.message}")
            }
        }
        Result.success(Unit)
    }

    suspend fun createFile(
        projectId: String,
        name: String,
        content: String = "",
        isFolder: Boolean = false
    ): ProjectFile = withContext(Dispatchers.IO) {
        val fileId = "file_${UUID.randomUUID().toString().take(8)}"
        val uid = currentUserId()
        val language = detectLanguage(name)

        val newFile = ProjectFile(
            id = fileId,
            projectId = projectId,
            userId = uid,
            name = name,
            path = name,
            content = content,
            language = language,
            isFolder = isFolder
        )

        val files = (localFiles.value[projectId] ?: emptyList()).toMutableList()
        files.add(newFile)
        val updatedMap = localFiles.value.toMutableMap()
        updatedMap[projectId] = files
        localFiles.value = updatedMap

        val authUid = auth.currentUser?.uid
        if (authUid != null) {
            try {
                val payload = mapOf(
                    "id" to fileId,
                    "projectId" to projectId,
                    "userId" to authUid,
                    "name" to name,
                    "path" to name,
                    "content" to content,
                    "language" to language,
                    "isFolder" to isFolder,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                db.collection("users").document(authUid).collection("projects").document(projectId).collection("files").document(fileId).set(payload).await()
            } catch (e: Exception) {
                Log.w(TAG, "Cloud sync createFile error: ${e.message}")
            }
        }
        newFile
    }

    suspend fun deleteFile(projectId: String, fileId: String) = withContext(Dispatchers.IO) {
        val files = (localFiles.value[projectId] ?: emptyList()).filterNot { it.id == fileId }
        val updatedMap = localFiles.value.toMutableMap()
        updatedMap[projectId] = files
        localFiles.value = updatedMap

        val authUid = auth.currentUser?.uid
        if (authUid != null) {
            try {
                db.collection("users").document(authUid).collection("projects").document(projectId).collection("files").document(fileId).delete().await()
            } catch (e: Exception) {
                Log.w(TAG, "Cloud sync deleteFile error: ${e.message}")
            }
        }
    }

    suspend fun deleteProject(projectId: String) = withContext(Dispatchers.IO) {
        localProjects.value = localProjects.value.filterNot { it.id == projectId }
        val updatedMap = localFiles.value.toMutableMap()
        updatedMap.remove(projectId)
        localFiles.value = updatedMap

        val authUid = auth.currentUser?.uid
        if (authUid != null) {
            try {
                db.collection("users").document(authUid).collection("projects").document(projectId).delete().await()
            } catch (e: Exception) {
                Log.w(TAG, "Cloud sync deleteProject error: ${e.message}")
            }
        }
    }

    suspend fun duplicateProject(projectId: String): Project? = withContext(Dispatchers.IO) {
        val original = getProject(projectId) ?: return@withContext null
        val files = getProjectFiles(projectId)
        val copy = createProject(
            name = "${original.name} (Copy)",
            description = original.description,
            template = original.template
        )
        for (f in files) {
            if (f.name != "index.html" && f.name != "main.py" && f.name != "App.jsx") {
                createFile(copy.id, f.name, f.content, f.isFolder)
            } else {
                val copyFiles = localFiles.value[copy.id] ?: emptyList()
                val targetFile = copyFiles.find { it.name == f.name }
                if (targetFile != null) {
                    saveFileContent(copy.id, targetFile.id, f.content)
                }
            }
        }
        copy
    }

    fun exportProjectZip(projectId: String): ByteArray {
        val files = localFiles.value[projectId] ?: emptyList()
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            for (file in files) {
                if (!file.isFolder) {
                    val entry = ZipEntry(file.name)
                    zos.putNextEntry(entry)
                    zos.write(file.content.toByteArray(Charsets.UTF_8))
                    zos.closeEntry()
                }
            }
        }
        return baos.toByteArray()
    }

    suspend fun importProjectFromZip(name: String, zipBytes: ByteArray): Project = withContext(Dispatchers.IO) {
        val project = createProject(name = name, template = "blank")
        val createdFiles = mutableListOf<ProjectFile>()
        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val content = zis.readBytes().toString(Charsets.UTF_8)
                    val newFile = createFile(project.id, entry.name, content)
                    createdFiles.add(newFile)
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        project
    }

    private fun detectLanguage(fileName: String): String {
        return when {
            fileName.endsWith(".html") || fileName.endsWith(".htm") -> "html"
            fileName.endsWith(".css") -> "css"
            fileName.endsWith(".js") -> "javascript"
            fileName.endsWith(".ts") -> "typescript"
            fileName.endsWith(".jsx") -> "jsx"
            fileName.endsWith(".tsx") -> "tsx"
            fileName.endsWith(".py") -> "python"
            fileName.endsWith(".json") -> "json"
            fileName.endsWith(".java") -> "java"
            fileName.endsWith(".c") -> "c"
            fileName.endsWith(".cpp") -> "cpp"
            fileName.endsWith(".md") -> "markdown"
            fileName.endsWith(".xml") -> "xml"
            fileName.endsWith(".sql") -> "sql"
            else -> "text"
        }
    }

    private fun generateStarterFiles(projectId: String, uid: String, template: String): List<ProjectFile> {
        val files = mutableListOf<ProjectFile>()
        when (template.lowercase()) {
            "html" -> {
                files.add(
                    ProjectFile(
                        id = "file_${UUID.randomUUID().toString().take(8)}",
                        projectId = projectId,
                        userId = uid,
                        name = "index.html",
                        path = "index.html",
                        language = "html",
                        content = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Natnael Code App</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div class="container">
    <div class="card">
      <div class="badge">Natnael Code Mobile Studio</div>
      <h1>Hello, World!</h1>
      <p class="subtitle">Edit HTML, CSS, and JS with instant live preview on mobile.</p>
      
      <div class="interactive-box">
        <button id="counterBtn" class="btn primary">Clicks: <span id="count">0</span></button>
        <button id="colorBtn" class="btn secondary">Change Gradient</button>
      </div>

      <div class="code-tag">
        <code>console.log("Natnael Code running smoothly!")</code>
      </div>
    </div>
  </div>
  <script src="script.js"></script>
</body>
</html>"""
                    )
                )
                files.add(
                    ProjectFile(
                        id = "file_${UUID.randomUUID().toString().take(8)}",
                        projectId = projectId,
                        userId = uid,
                        name = "style.css",
                        path = "style.css",
                        language = "css",
                        content = """* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

body {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #0d1117;
  color: #c9d1d9;
  padding: 20px;
}

.container {
  width: 100%;
  max-width: 420px;
}

.card {
  background: #161b22;
  border: 1px solid #30363d;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 16px 36px rgba(0, 0, 0, 0.4);
  text-align: center;
}

.badge {
  display: inline-block;
  background: rgba(88, 166, 255, 0.15);
  color: #58a6ff;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  padding: 6px 14px;
  border-radius: 100px;
  margin-bottom: 16px;
  border: 1px solid rgba(88, 166, 255, 0.3);
}

h1 {
  font-size: 28px;
  font-weight: 800;
  background: linear-gradient(135deg, #58a6ff 0%, #bc8cff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 10px;
}

.subtitle {
  font-size: 14px;
  color: #8b949e;
  line-height: 1.5;
  margin-bottom: 24px;
}

.interactive-box {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-bottom: 24px;
}

.btn {
  padding: 12px 18px;
  font-size: 14px;
  font-weight: 600;
  border-radius: 12px;
  cursor: pointer;
  border: none;
  transition: transform 0.1s ease;
}

.btn:active {
  transform: scale(0.96);
}

.btn.primary {
  background: #58a6ff;
  color: #0d1117;
}

.btn.secondary {
  background: #21262d;
  color: #c9d1d9;
  border: 1px solid #30363d;
}

.code-tag {
  background: #090d13;
  padding: 12px;
  border-radius: 10px;
  border: 1px dashed #30363d;
  font-size: 12px;
  color: #7ee787;
  overflow-x: auto;
}"""
                    )
                )
                files.add(
                    ProjectFile(
                        id = "file_${UUID.randomUUID().toString().take(8)}",
                        projectId = projectId,
                        userId = uid,
                        name = "script.js",
                        path = "script.js",
                        language = "javascript",
                        content = """console.log("Natnael Code: script loaded successfully!");

let count = 0;
const countSpan = document.getElementById("count");
const counterBtn = document.getElementById("counterBtn");
const colorBtn = document.getElementById("colorBtn");

counterBtn.addEventListener("click", () => {
  count++;
  countSpan.textContent = count;
  console.log("Button clicked, count is now:", count);
});

const gradients = [
  "linear-gradient(135deg, #58a6ff 0%, #bc8cff 100%)",
  "linear-gradient(135deg, #3fb950 0%, #58a6ff 100%)",
  "linear-gradient(135deg, #d29922 0%, #f85149 100%)",
  "linear-gradient(135deg, #f778ba 0%, #a371f7 100%)"
];

let gradIndex = 0;
colorBtn.addEventListener("click", () => {
  gradIndex = (gradIndex + 1) % gradients.length;
  document.querySelector("h1").style.background = gradients[gradIndex];
  document.querySelector("h1").style.webkitBackgroundClip = "text";
  console.log("Changed gradient index to:", gradIndex);
});"""
                    )
                )
            }
            "react" -> {
                files.add(
                    ProjectFile(
                        id = "file_${UUID.randomUUID().toString().take(8)}",
                        projectId = projectId,
                        userId = uid,
                        name = "App.jsx",
                        path = "App.jsx",
                        language = "jsx",
                        content = """import React, { useState } from 'react';

export default function App() {
  const [tasks, setTasks] = useState([
    { id: 1, text: "Build Natnael Code Editor", done: true },
    { id: 2, text: "Optimize Mobile Keyboard", done: true },
    { id: 3, text: "Test AI Code Generation", done: false }
  ]);
  const [input, setInput] = useState("");

  const addTask = () => {
    if (!input.trim()) return;
    setTasks([...tasks, { id: Date.now(), text: input, done: false }]);
    setInput("");
  };

  const toggleTask = (id) => {
    setTasks(tasks.map(t => t.id === id ? { ...t, done: !t.done } : t));
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h2 style={styles.title}>React Mobile App</h2>
        <p style={styles.subtitle}>Powered by Natnael Code</p>

        <div style={styles.inputRow}>
          <input
            style={styles.input}
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Add new task..."
          />
          <button style={styles.addBtn} onClick={addTask}>Add</button>
        </div>

        <div style={styles.list}>
          {tasks.map(t => (
            <div 
              key={t.id} 
              style={{...styles.item, opacity: t.done ? 0.6 : 1}}
              onClick={() => toggleTask(t.id)}
            >
              <span>{t.done ? "✓" : "○"} {t.text}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

const styles = {
  container: { minHeight: '100vh', background: '#0d1117', color: '#c9d1d9', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '16px' },
  card: { background: '#161b22', border: '1px solid #30363d', borderRadius: '16px', padding: '24px', width: '100%', maxWidth: '380px' },
  title: { fontSize: '22px', fontWeight: 'bold', color: '#58a6ff', margin: 0 },
  subtitle: { color: '#8b949e', fontSize: '13px', marginBottom: '16px' },
  inputRow: { display: 'flex', gap: '8px', marginBottom: '16px' },
  input: { flex: 1, background: '#090d13', border: '1px solid #30363d', borderRadius: '8px', padding: '10px', color: '#fff', fontSize: '14px' },
  addBtn: { background: '#238636', color: '#fff', border: 'none', borderRadius: '8px', padding: '0 16px', fontWeight: 'bold', cursor: 'pointer' },
  list: { display: 'flex', flexDirection: 'column', gap: '8px' },
  item: { background: '#21262d', padding: '12px', borderRadius: '8px', cursor: 'pointer', border: '1px solid #30363d' }
};"""
                    )
                )
                files.add(
                    ProjectFile(
                        id = "file_${UUID.randomUUID().toString().take(8)}",
                        projectId = projectId,
                        userId = uid,
                        name = "index.html",
                        path = "index.html",
                        language = "html",
                        content = """<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>React App</title>
  <script src="https://unpkg.com/react@18/umd/react.production.min.js"></script>
  <script src="https://unpkg.com/react-dom@18/umd/react-dom.production.min.js"></script>
  <script src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
</head>
<body style="margin:0; background:#0d1117;">
  <div id="root"></div>
  <script type="text/babel">
    // App component rendered here
  </script>
</body>
</html>"""
                    )
                )
            }
            "python" -> {
                files.add(
                    ProjectFile(
                        id = "file_${UUID.randomUUID().toString().take(8)}",
                        projectId = projectId,
                        userId = uid,
                        name = "main.py",
                        path = "main.py",
                        language = "python",
                        content = """# Natnael Code Python Sandbox
import math
import sys

def banner():
    print("====================================")
    print("      NATNAEL CODE PYTHON 3.12      ")
    print("   Code Anywhere. Build Anything.   ")
    print("====================================\n")

def fibonacci(n):
    fib = [0, 1]
    for i in range(2, n):
        fib.append(fib[-1] + fib[-2])
    return fib

def is_prime(num):
    if num < 2: return False
    for i in range(2, int(math.isqrt(num)) + 1):
        if num % i == 0: return False
    return True

def main():
    banner()
    
    # Fibonacci calculation
    terms = 10
    fib_series = fibonacci(terms)
    print(f"Fibonacci series (first {terms} terms):")
    print(f" -> {fib_series}\n")

    # Prime numbers sieve
    primes = [x for x in range(2, 50) if is_prime(x)]
    print("Primes below 50:")
    print(f" -> {primes}\n")

    print("Success: Script executed without errors.")

if __name__ == "__main__":
    main()"""
                    )
                )
            }
            else -> {
                files.add(
                    ProjectFile(
                        id = "file_${UUID.randomUUID().toString().take(8)}",
                        projectId = projectId,
                        userId = uid,
                        name = "main.js",
                        path = "main.js",
                        language = "javascript",
                        content = """console.log("Hello from Natnael Code!");
"""
                    )
                )
            }
        }
        return files
    }

    private fun createSampleProjectsIfEmpty() {
        if (localProjects.value.isNotEmpty()) return

        val p1 = Project(
            id = "proj_demo_html",
            userId = "local_guest",
            name = "Interactive Portfolio",
            description = "Modern responsive mobile portfolio with live preview",
            template = "html",
            language = "html"
        )
        val p2 = Project(
            id = "proj_demo_py",
            userId = "local_guest",
            name = "Python Algorithms",
            description = "Math, primes, data structures, and terminal output",
            template = "python",
            language = "python"
        )
        val p3 = Project(
            id = "proj_demo_react",
            userId = "local_guest",
            name = "React Mobile Tasker",
            description = "Interactive React task management app",
            template = "react",
            language = "jsx"
        )

        localProjects.value = listOf(p1, p2, p3)
        val filesMap = mutableMapOf<String, List<ProjectFile>>()
        filesMap[p1.id] = generateStarterFiles(p1.id, "local_guest", "html")
        filesMap[p2.id] = generateStarterFiles(p2.id, "local_guest", "python")
        filesMap[p3.id] = generateStarterFiles(p3.id, "local_guest", "react")
        localFiles.value = filesMap
    }
}
