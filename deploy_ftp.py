import os
import sys
import ftplib

FTP_HOST = "ftp.zerolord.com"
FTP_PORT = 21
FTP_USER = "cinback@zerolord.com"
FTP_PASS = "@f33rinimi"
LOCAL_DIR = os.path.join(os.getcwd(), "backend")

def get_ftp_connection():
    print(f"Connecting to FTP server {FTP_HOST}:{FTP_PORT}...")
    try:
        ftp = ftplib.FTP_TLS()
        ftp.connect(FTP_HOST, FTP_PORT, timeout=15)
        ftp.login(FTP_USER, FTP_PASS)
        ftp.prot_p()
        print("Connected using Explicit FTPS (TLS).")
        return ftp
    except Exception as e:
        print(f"FTPS failed: {e}. Falling back to standard FTP...")
        ftp = ftplib.FTP()
        ftp.connect(FTP_HOST, FTP_PORT, timeout=15)
        ftp.login(FTP_USER, FTP_PASS)
        print("Connected using standard FTP.")
        return ftp

def upload_directory(ftp, local_dir, remote_dir):
    print(f"\nProcessing local directory: {local_dir}")
    print(f"Current remote directory: {ftp.pwd()}")

    for item in os.listdir(local_dir):
        local_path = os.path.join(local_dir, item)
        if os.path.isdir(local_path):
            try:
                ftp.mkd(item)
                print(f"Created remote directory: {item}")
            except ftplib.error_perm as e:
                # Directory probably exists
                print(f"Directory {item} exists or creation warning: {e}")
            
            ftp.cwd(item)
            upload_directory(ftp, local_path, os.path.join(remote_dir, item))
            ftp.cwd("..")
        else:
            print(f"Uploading file: {item} -> {os.path.join(remote_dir, item)}...")
            with open(local_path, "rb") as f:
                ftp.storbinary(f"STOR {item}", f)
            print(f"✓ Uploaded {item} ({os.path.getsize(local_path)} bytes)")

def main():
    if not os.path.exists(LOCAL_DIR):
        print(f"Error: Local directory {LOCAL_DIR} does not exist.")
        sys.exit(1)

    ftp = get_ftp_connection()
    print("Initial working directory:", ftp.pwd())

    upload_directory(ftp, LOCAL_DIR, ftp.pwd())

    print("\n--- Listing remote uploaded files ---")
    ftp.dir()
    
    ftp.quit()
    print("\nFTP Deployment Completed Successfully!")

if __name__ == "__main__":
    main()
