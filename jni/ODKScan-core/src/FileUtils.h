#include <string>
#include <vector>

std::string addSlashIfNeeded(const std::string& str);
// crawls a directory rootdir for filenames and appends them to the filenames
// vector parameter (modifies filenames vector)
int CrawlFileTree(std::string rootdir, std::vector<std::string> &filenames);
int CrawlFileTree(char* rootdir, std::vector<std::string > &filenames);
bool fileExists(const std::string& filename);
bool isImage(const std::string& filename);
