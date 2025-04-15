we should improve the upload logic a little bit for better maintainability, scalability, and clarity.

Tasks being

Organize uploads into subfolders

Store files under root/subfolder/filename instead of directly in the root.

Ensure subfolders are created if they don't exist.

Update UserService.uploadProfilePicture() to pass "users" to the storage service.

Update PetService.uploadProfilePicture() to pass "pets".

Extract duplicate file deletion logic

Add a helper method like deleteExistingFile(String path) in both UserService and PetService.

Replace repeated deletion logic with this helper.

Add validation tests in FileStorageServiceTest

Test rejection of files that exceed the configured max file size.

Test rejection of files with disallowed MIME types.