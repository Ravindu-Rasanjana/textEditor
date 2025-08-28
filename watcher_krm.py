import os
import time
import subprocess
import threading
from datetime import datetime
import tkinter as tk
from tkinter import scrolledtext, ttk

TRIGGER_FILE = "C:/Users/Ravindu/AndroidStudioProjects/Compiles/run.txt"
SOURCE_DIR = "C:/Users/Ravindu/AndroidStudioProjects/Compiles/"
PHONE_CODE_DIR = "/storage/emulated/0/Android/data/com.example.texteditor/files/codes"

class CodeWatcherApp:
    def __init__(self, root):
        self.root = root
        self.root.title("Mobile Code Execution Watcher")
        self.root.geometry("800x600")
        self.root.configure(bg="#2b2b2b")
        
        self.is_watching = False
        self.watch_thread = None
        
        self.setup_ui()
        self.log("🚀 Application started. Click 'Start Watching' to begin.")
        
    def setup_ui(self):
        # Header
        header_frame = tk.Frame(self.root, bg="#2b2b2b")
        header_frame.pack(pady=10)
        
        self.title_label = tk.Label(
            header_frame, 
            text="Mobile Code Execution Watcher", 
            font=("Arial", 16, "bold"),
            fg="#00ff00",
            bg="#2b2b2b"
        )
        self.title_label.pack()
        
        # Status
        self.status_label = tk.Label(
            header_frame,
            text="Status: Not Watching",
            font=("Arial", 10),
            fg="#ff5555",
            bg="#2b2b2b"
        )
        self.status_label.pack(pady=5)
        
        # Controls
        control_frame = tk.Frame(self.root, bg="#2b2b2b")
        control_frame.pack(pady=10)
        
        self.start_btn = tk.Button(
            control_frame,
            text="Start Watching",
            command=self.start_watching,
            bg="#4CAF50",
            fg="white",
            font=("Arial", 10, "bold"),
            width=15
        )
        self.start_btn.pack(side=tk.LEFT, padx=5)
        
        self.stop_btn = tk.Button(
            control_frame,
            text="Stop Watching",
            command=self.stop_watching,
            bg="#f44336",
            fg="white",
            font=("Arial", 10, "bold"),
            width=15,
            state=tk.DISABLED
        )
        self.stop_btn.pack(side=tk.LEFT, padx=5)
        
        self.clear_btn = tk.Button(
            control_frame,
            text="Clear Log",
            command=self.clear_log,
            bg="#2196F3",
            fg="white",
            font=("Arial", 10, "bold"),
            width=15
        )
        self.clear_btn.pack(side=tk.LEFT, padx=5)
        
        # Log area
        log_frame = tk.Frame(self.root, bg="#2b2b2b")
        log_frame.pack(fill=tk.BOTH, expand=True, padx=10, pady=10)
        
        self.log_area = scrolledtext.ScrolledText(
            log_frame,
            wrap=tk.WORD,
            width=80,
            height=20,
            bg="#1e1e1e",
            fg="#d4d4d4",
            insertbackground="white",
            font=("Consolas", 10)
        )
        self.log_area.pack(fill=tk.BOTH, expand=True)
        
        # Footer
        footer_frame = tk.Frame(self.root, bg="#2b2b2b")
        footer_frame.pack(side=tk.BOTTOM, fill=tk.X, pady=5)
        
        self.footer_label = tk.Label(
            footer_frame,
            text="Developed for Mobile Code Execution",
            font=("Arial", 8),
            fg="#aaaaaa",
            bg="#2b2b2b"
        )
        self.footer_label.pack()
        
    def log(self, message):
        timestamp = datetime.now().strftime("%H:%M:%S")
        formatted_message = f"[{timestamp}] {message}"
        
        self.log_area.configure(state=tk.NORMAL)
        self.log_area.insert(tk.END, formatted_message + "\n")
        self.log_area.see(tk.END)
        self.log_area.configure(state=tk.DISABLED)
        
    def clear_log(self):
        self.log_area.configure(state=tk.NORMAL)
        self.log_area.delete(1.0, tk.END)
        self.log_area.configure(state=tk.DISABLED)
        self.log("Log cleared")
        
    def start_watching(self):
        self.is_watching = True
        self.start_btn.configure(state=tk.DISABLED)
        self.stop_btn.configure(state=tk.NORMAL)
        self.status_label.configure(text="Status: Watching", fg="#00ff00")
        self.log("🔥 Watcher started. Waiting for trigger...")
        
        # Start watching in a separate thread
        self.watch_thread = threading.Thread(target=self.watch_for_triggers, daemon=True)
        self.watch_thread.start()
        
    def stop_watching(self):
        self.is_watching = False
        self.start_btn.configure(state=tk.NORMAL)
        self.stop_btn.configure(state=tk.DISABLED)
        self.status_label.configure(text="Status: Not Watching", fg="#ff5555")
        self.log("⏹️ Watcher stopped")
        
    def run_and_capture(self, command, output_path, shell=False):
        result = subprocess.run(command, capture_output=True, text=True, shell=shell)
        with open(output_path, "w", encoding="utf-8") as out_file:
            if result.stdout:
                out_file.write(result.stdout)
            if result.stderr:
                out_file.write(result.stderr)
        return result.returncode
    
    def watch_for_triggers(self):
        while self.is_watching:
            try:
                # Create directory if it doesn't exist
                os.makedirs(os.path.dirname(TRIGGER_FILE), exist_ok=True)
                
                # Try to pull the trigger file
                result = subprocess.run(
                    ["adb", "pull", f"{PHONE_CODE_DIR}/run.txt", TRIGGER_FILE],
                    capture_output=True,
                    text=True
                )
                
                if result.returncode != 0:
                    time.sleep(5)
                    continue
                    
                if os.path.exists(TRIGGER_FILE):
                    try:
                        with open(TRIGGER_FILE, 'r') as f:
                            filename = f.read().strip()

                        file_path = os.path.join(SOURCE_DIR, filename)
                        name_only, ext = os.path.splitext(filename)
                        output_txt = os.path.join(SOURCE_DIR, f"{name_only}.txt")

                        self.log(f"🚀 Triggered for: {filename}")
                        self.log(f"🛠️ DEBUG: Expected local file path = {file_path}")

                        self.log(f"⬇️ Pulling {filename} from phone...")
                        
                        # Pull the file with proper extension handling
                        pull_result = subprocess.run(
                            ["adb", "pull", f"{PHONE_CODE_DIR}/{filename}", file_path],
                            capture_output=True,
                            text=True
                        )
                        
                        if pull_result.returncode != 0:
                            self.log(f"❌ Failed to pull file: {pull_result.stderr}")
                            with open(output_txt, "w", encoding="utf-8") as f:
                                f.write("❌ File not found after pull!")
                            subprocess.run(["adb", "push", output_txt, f"{PHONE_CODE_DIR}/{name_only}.txt"], check=True)
                        else:
                            # Check if file exists with the correct extension
                            if not os.path.exists(file_path):
                                # Try to find the file without extension
                                files = [f for f in os.listdir(SOURCE_DIR) if f.startswith(name_only)]
                                if files:
                                    file_path = os.path.join(SOURCE_DIR, files[0])
                                    self.log(f"🔍 Found file without extension: {files[0]}")
                                else:
                                    with open(output_txt, "w", encoding="utf-8") as f:
                                        f.write("❌ File not found after pull!")
                                    subprocess.run(["adb", "push", output_txt, f"{PHONE_CODE_DIR}/{name_only}.txt"], check=True)
                                    continue

                            try:
                                if ext == ".java":
                                    self.log("🔨 Compiling Java code...")
                                    compile_result = subprocess.run(["javac", file_path], capture_output=True, text=True)
                                    if compile_result.returncode != 0:
                                        with open(output_txt, "w", encoding="utf-8") as f:
                                            f.write(compile_result.stderr)
                                        self.log("❌ Java compilation failed")
                                    else:
                                        self.run_and_capture(["java", "-cp", SOURCE_DIR, name_only], output_txt)
                                        self.log("✅ Java execution completed")

                                elif ext == ".py":
                                    self.log("🐍 Executing Python code...")
                                    self.run_and_capture(["py", file_path], output_txt)
                                    self.log("✅ Python execution completed")

                                elif ext == ".c":
                                    self.log("🔨 Compiling C code...")
                                    exe_path = os.path.join(SOURCE_DIR, f"{name_only}.exe")
                                    compile_result = subprocess.run(["gcc", file_path, "-o", exe_path], capture_output=True, text=True)
                                    if compile_result.returncode != 0:
                                        with open(output_txt, "w", encoding="utf-8") as f:
                                            f.write(compile_result.stderr)
                                        self.log("❌ C compilation failed")
                                    else:
                                        self.run_and_capture([exe_path], output_txt)
                                        self.log("✅ C execution completed")

                                elif ext == ".cpp":
                                    self.log("🔨 Compiling C++ code...")
                                    exe_path = os.path.join(SOURCE_DIR, f"{name_only}.exe")
                                    compile_result = subprocess.run(["g++", file_path, "-o", exe_path], capture_output=True, text=True)
                                    if compile_result.returncode != 0:
                                        with open(output_txt, "w", encoding="utf-8") as f:
                                            f.write(compile_result.stderr)
                                        self.log("❌ C++ compilation failed")
                                    else:
                                        self.run_and_capture([exe_path], output_txt)
                                        self.log("✅ C++ execution completed")

                                elif ext == ".kt":
                                    self.log("🔨 Compiling Kotlin code...")
                                    jar_path = os.path.join(SOURCE_DIR, f"{name_only}.jar")
                                    compile_result = subprocess.run(["kotlinc", file_path, "-include-runtime", "-d", jar_path], capture_output=True, text=True)
                                    if compile_result.returncode != 0:
                                        with open(output_txt, "w", encoding="utf-8") as f:
                                            f.write(compile_result.stderr)
                                        self.log("❌ Kotlin compilation failed")
                                    else:
                                        self.run_and_capture(["java", "-jar", jar_path], output_txt)
                                        self.log("✅ Kotlin execution completed")

                                else:
                                    with open(output_txt, "w", encoding="utf-8") as f:
                                        f.write(f"❓ Unsupported file type: {ext}")
                                    self.log(f"⚠️ Unsupported file type: {ext}")

                            except Exception as e:
                                with open(output_txt, "w", encoding="utf-8") as f:
                                    f.write(f"💥 Error: {str(e)}")
                                self.log(f"💥 Error during execution: {str(e)}")

                            # Push the result back to the phone
                            subprocess.run(["adb", "push", output_txt, f"{PHONE_CODE_DIR}/{name_only}.txt"], check=True)
                            self.log(f"📤 Results pushed to phone: {name_only}.txt")

                    except Exception as ex:
                        self.log(f"⚠️ General Error: {ex}")

                    # Clean up
                    if os.path.exists(TRIGGER_FILE):
                        os.remove(TRIGGER_FILE)
                    self.log("✅ Done. Waiting for next trigger...\n")

            except Exception as e:
                self.log(f"❌ Error in watch loop: {str(e)}")
                
            time.sleep(5)

def main():
    root = tk.Tk()
    app = CodeWatcherApp(root)
    root.mainloop()

if __name__ == "__main__":
    main()