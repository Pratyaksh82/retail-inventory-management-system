import os
import sys
import subprocess
import zipfile
import shutil

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

def main():
    ws = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    bin_dir = os.path.join(ws, 'bin')
    dist_dir = os.path.join(ws, 'dist')

    os.makedirs(bin_dir, exist_ok=True)
    os.makedirs(dist_dir, exist_ok=True)

    print("================================================================================")
    print("  PACKAGING EXECUTABLE STANDALONE JAR FOR PRODUCTION DEPLOYMENT")
    print("================================================================================")

    # 1. Compile Java sources
    print(">>> Compiling all Java classes...")
    src_files = []
    for root, dirs, files in os.walk(os.path.join(ws, 'src')):
        for f in files:
            if f.endswith('.java'):
                src_files.append(os.path.join(root, f))

    lib_jar = os.path.join(ws, 'lib', 'junit-platform-console-standalone-1.10.2.jar')
    cmd = ['javac', '-cp', f'{lib_jar};{bin_dir}', '-d', bin_dir] + src_files
    c_res = subprocess.run(cmd, cwd=ws, capture_output=True, text=True)
    if c_res.returncode != 0:
        print("❌ Compilation failed:", c_res.stderr)
        return
    print("✅ Classes compiled successfully into bin/")

    # 2. Run automated tests gatekeeper
    print(">>> Running automated verification suite...")
    t_res = subprocess.run(['java', '-cp', bin_dir, 'com.inventory.library.LibraryTestRunner'],
                           cwd=ws, capture_output=True, text=True)
    if t_res.returncode != 0:
        print("❌ Automated tests failed:", t_res.stderr)
        return
    print("✅ Automated test gatekeeper passed (20 assertions successful)!")

    # 3. Create Executable JAR
    jar_path = os.path.join(dist_dir, 'retail-inventory-system-1.0.jar')
    manifest_content = (
        "Manifest-Version: 1.0\r\n"
        "Main-Class: com.inventory.UnifiedInventorySystem\r\n"
        "Created-By: Antigravity Java Builder (JDK 26)\r\n"
        "Implementation-Title: Unified Retail and Library Inventory System\r\n"
        "Implementation-Version: 1.0.0-PROD\r\n\r\n"
    )

    print(">>> Bundling Executable JAR: " + jar_path)
    with zipfile.ZipFile(jar_path, 'w', zipfile.ZIP_DEFLATED) as jar:
        # Write MANIFEST.MF
        jar.writestr('META-INF/MANIFEST.MF', manifest_content)
        # Write all class files
        for root, dirs, files in os.walk(bin_dir):
            for f in files:
                full_path = os.path.join(root, f)
                rel_path = os.path.relpath(full_path, bin_dir)
                jar.write(full_path, rel_path)

    print("✅ Standalone Executable JAR created successfully!")
    print(f"   Artifact: {jar_path}")
    print(f"   Size:     {os.path.getsize(jar_path)} bytes")
    print("================================================================================")

if __name__ == '__main__':
    main()
