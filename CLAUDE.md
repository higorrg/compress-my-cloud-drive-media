# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

A Java CLI tool that manages the size of cloud drive storage — originally built to compress video/image/PDF files stored on Google Drive to cut storage costs. It lists files, exports an inventory to CSV, and can download, compress (via ffmpeg through JavaCV), and re-upload media files, all through a single `--cloud-drive=google` flag today, though the design leaves room for other providers.

## Commands

- Build/package (produces the shaded runnable jar): `mvn -B package`
- Run all tests: `mvn test`
- Run a single test class: `mvn test -Dtest=FileExtensionFixerTest`
- Run a single test method: `mvn test -Dtest=FileExtensionFixerTest#testFixFileExtensionIfNull_AddsExtensionWhenMp4IsMissing`
- Run directly via Maven (uses `exec-maven-plugin`, `main.class` in `pom.xml`): `mvn exec:java -Dexec.args="--list --cloud-drive=google"`
- Run the packaged jar: `java -jar target/cloud-drive-compressor-1.0.0-runner.jar --list --cloud-drive=google`

CI (`.github/workflows/maven.yml`) runs `mvn -B package` on JDK 21 for pushes/PRs to `main`.

Requires JDK 21 and system `ffmpeg` and `gs` (Ghostscript) binaries on PATH — video/image compression shells out to `ffmpeg` directly, PDF compression shells out to `gs` directly. Both are also required to run the test suite (`PdfCompressorHandlerTest` and `MediaPipelineIdCollisionIntegrationTest` shell out to the real binaries to generate fixtures and exercise the handlers). Google Drive auth expects OAuth client secrets at `~/credentials/client_secret.json`; tokens are cached under `./tokens` (gitignored) after the first OAuth flow.

## Architecture

The core design is a mix of **Chain of Responsibility** and **Observer**, chosen specifically so that files can be processed (downloaded/compressed/uploaded) as they're paginated in from the cloud API, rather than requiring a full file listing before any processing starts. See `README.md` for the rationale and C4 diagrams under `diagram/`.

### Package structure (`br.com.granzoto.media_compressor`)

- **`cloud_client`** — Provider-agnostic contracts. `CloudClient` is the subject; `AbstractCloudClient` implements the observer registries (`CloudClientStartObserver`, `CloudClientItemObserver`, `CloudClientEndObserver`) and drives pagination via the abstract `listFilesByPage`. `CloudClientHandler` is what plugs into a client (`registerObserver`) to become one or more of those observer types.
- **`cloud_client_for_google`** — The only current provider implementation. `GoogleDriveClient` (singleton) extends `AbstractCloudClient`, paginates Drive's `files.list`, separates folders from files (folders populate a `folderId -> FolderInfo` map used to resolve paths), and implements `downloadFile`/`uploadFile`. `GoogleDriveAuth` handles the OAuth installed-app flow. `CompressionFileFromGoogleFileFactory` maps a raw Google `File` into the domain `CompressionFile` record.
- **`cloud_client_observer_handler`** — The pluggable pipeline stages, each a `CloudClientHandler` + one or more observer interfaces: `FileListToLogHandler`, `FileListToCsvHandler`, `DownloadHandler`, `VideoCompressorHandler` (shells out to `ffmpeg`), `ImageCompressorHandler` (also `ffmpeg`), `PdfCompressorHandler` (shells out to `gs`/Ghostscript), `UploadHandler`. Handlers are independent and only act on files matching their concern (e.g. compressors check `compressionFile.mimeSuperType()` and skip if the output already exists).
- **`model`** — Domain types and pure helpers: `CompressionFile` (the record threaded through the whole pipeline: id, name, size, folderPath, mime info, original/compressed `File` handles), `FolderInfo`, `FolderPathResolver` (walks the folder map to build a path), `FileExtensionFixer` (ffmpeg needs a real extension to probe mime type, so this backfills one from the mime type when missing), `MimeSuperTypeExtractor`, `FileFactory` (computes original/compressed file locations under the user's download folder), and `UserOptions` (singleton picocli-backed CLI args).
- **`main`** — Wiring only. `CloudClientFactory` maps `--cloud-drive` to a `CloudClient` instance. `HandlerFactory` lazily registers handlers (keyed by CLI flag name) onto the client, using `Consumer<CloudClient>` so unused handlers are never instantiated. `Main` parses args with picocli, wires everything, then calls `cloudClient.runFiles()`.

### Adding a new cloud provider

Implement `CloudClient` (typically by extending `AbstractCloudClient` and implementing `listFilesByPage`, `downloadFile`, `uploadFile`), then register it in `CloudClientFactory`. Existing handlers in `cloud_client_observer_handler` work unmodified against any provider since they only depend on `CloudClient`/`CompressionFile`.

### Adding a new handler/pipeline stage

Implement `CloudClientHandler` plus whichever observer interface(s) fit (`CloudClientStartObserver`/`CloudClientItemObserver`/`CloudClientEndObserver`), register it with a `Consumer<CloudClient>` entry in `HandlerFactory`, and expose a corresponding flag on `UserOptions`.

## Code style

`eclipse-formatter.xml` defines the project's formatting rules for IDE import.
