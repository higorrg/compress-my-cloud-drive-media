from pydrive2.auth import GoogleAuth
from pydrive2.drive import GoogleDrive
from moviepy.video.io.VideoFileClip import VideoFileClip
import os

def authenticate_google_drive():
    gauth = GoogleAuth()
    # Use local webserver and create settings.yaml for seamless authentication
    gauth.LocalWebserverAuth()
    drive = GoogleDrive(gauth)
    return drive

def list_video_files(drive, folder_id=None):
    query = "mimeType contains 'video/'"
    if folder_id:
        query += f" and '{folder_id}' in parents"

    file_list = drive.ListFile({'q': query}).GetList()
    return file_list

def compress_video(input_path, output_path, target_resolution=(1280, 720)):
    try:
        clip = VideoFileClip(input_path)
        clip_resized = clip.resize(height=target_resolution[1])
        clip_resized.write_videofile(output_path, codec="libx264", audio_codec="aac")
        clip.close()
        return True
    except Exception as e:
        print(f"Error compressing video: {e}")
        return False

def download_file(file, local_path):
    file.GetContentFile(local_path)

def upload_file(drive, file_name, local_path, parent_id=None):
    new_file = drive.CreateFile({'title': file_name, 'parents': [{'id': parent_id}] if parent_id else []})
    new_file.SetContentFile(local_path)
    new_file.Upload()
    return new_file

def main():
    # Authenticate with Google Drive
    drive = authenticate_google_drive()

    # Set folder_id to restrict processing to a specific folder (None for root)
    folder_id = None  # Replace with your folder ID if needed

    # List all video files
    video_files = list_video_files(drive, folder_id)

    for file in video_files:
        print(f"Processing file: {file['title']}")
        
        # Paths for local processing
        original_path = os.path.join("temp", file['title'])
        compressed_path = os.path.join("temp", f"compressed_{file['title']}")
        
        # Download file
        download_file(file, original_path)
        
        # Compress video
        if compress_video(original_path, compressed_path):
            # Upload compressed file
            compressed_file = upload_file(drive, file['title'], compressed_path, parent_id=folder_id)
            
            # Delete original file from Google Drive
            file.Delete()
            print(f"Replaced original with compressed version: {file['title']}")
        else:
            print(f"Failed to compress file: {file['title']}")

        # Clean up local files
        os.remove(original_path)
        os.remove(compressed_path)

if __name__ == "__main__":
    main()

