import { deleteAllSessionFiles, deleteCacheFile, deleteUserCredFile } from "../../data-utils/CachingHelper.js";

export async function globalTeardown() {
  await Promise.all([
      deleteCacheFile(),
      deleteUserCredFile(),
      deleteAllSessionFiles()
  ]);
}
