import re
import requests
import json
import sys
import io
from bs4 import BeautifulSoup


class parseObj:
    repoName = ""
    author = ""
    about = ""
    fork = 0
    star = 0
    issue = 0
    mr = 0
    language = ""
    url = ""

    def __json__(self):
        return {
            'repoName': self.repoName,
            'about': self.about,
            'author': self.author,
            'fork': self.fork,
            'star': self.star,
            'issue': self.issue,
            'mr': self.mr,
            'language': self.language,
            'url': self.url
        }


if __name__ == '__main__':
    url = sys.argv[1]
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')
    header = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) "
                      "Chrome/125.0.0.0 Safari/537.36",
    }
    try:
        response = requests.get(url, headers=header)
        soup = BeautifulSoup(response.text, "html.parser")
        data = parseObj()
        data.url = url
        # about
        aboutTitle = soup.find(class_="f4 my-3")
        if aboutTitle:
            data.about = aboutTitle.text.strip()
        else:
            data.about = "None"

        data.author = soup.find(class_="author flex-self-stretch").find("a").text.strip()
        data.repoName = (data.author + "/" + soup.find(class_="mr-2 flex-self-stretch").find("a").text)
        data.fork = soup.find(id="repo-network-counter").text.strip()
        data.star = soup.find(id="repo-stars-counter-star").text.strip()

        # issue
        issueHtml = soup.find(id="issues-repo-tab-count")
        if issueHtml:
            data.issue = issueHtml.text.strip()

        # mr
        mrHtml = soup.find(id="pull-requests-repo-tab-count")
        if mrHtml:
            data.mr = mrHtml.text.strip()

        # language
        languageList = soup.find_all(class_="d-inline")
        languageRes = []
        if languageList:
            for item in languageList:
                languageInfo = item.find(class_="color-fg-default text-bold mr-1")
                if languageInfo:
                    languageRes.append(languageInfo.text.strip())

        if languageRes:
            data.language = ",".join(languageRes)
        else:
            data.language = "None"
        print(json.dumps(data.__json__(), ensure_ascii=False))
    except Exception as e:
        print(f'{"error": "{e}"}')
