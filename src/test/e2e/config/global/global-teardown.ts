import { deleteAllSessionFiles, deleteCacheFile, deleteUserCredFile } from "../../data-utils/CachingHelper.ts";

export default async function globalTeardown() {
  await Promise.all([
      deleteCacheFile(),
      deleteUserCredFile(),
      deleteAllSessionFiles()
  ]);
}
