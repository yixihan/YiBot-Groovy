import re
import requests
import json
import sys
import io
from bs4 import BeautifulSoup


class parseObj:
    img: ""
    title = ""
    author = ""
    # 视频简介
    desc = ""
    # 播放数
    watch = 0
    # 弹幕数
    barrage = 0
    # 点赞数
    like = 0
    # 收藏数
    collection = 0
    # 转发数
    share = 0
    # 投硬币数
    coin = 0
    # 链接
    avUrl = ""
    bvUrl = ""

    def __json__(self):
        return {
            'img': self.img,
            'title': self.title,
            'desc': self.desc,
            'author': self.author,
            'watch': self.watch,
            'barrage': self.barrage,
            'like': self.like,
            'collection': self.collection,
            'share': self.share,
            'coin': self.coin,
            'avUrl': self.avUrl,
            'bvUrl': self.bvUrl
        }


def get_video_url(id):
    if id.startswith('BV'):
        return f"https://www.bilibili.com/video/{id}"
    else:
        return f"https://www.bilibili.com/video/av{id}"


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

        # 视频 aid、视频时长和作者 id
        initial_state_script = soup.find("script", string=re.compile("window.__INITIAL_STATE__"))
        initial_state_text = initial_state_script.string

        author_id_pattern = re.compile(r'"mid":(\d+)')
        video_aid_pattern = re.compile(r'"aid":(\d+)')
        video_bvid_pattern = re.compile(r'"bvid":"(BV[1-9A-Za-z]+)"')
        video_duration_pattern = re.compile(r'"duration":(\d+)')

        author_id = author_id_pattern.search(initial_state_text).group(1)
        video_aid = video_aid_pattern.search(initial_state_text).group(1)
        video_bvid = video_bvid_pattern.search(initial_state_text).group(1)
        video_duration_raw = int(video_duration_pattern.search(initial_state_text).group(1))
        video_duration = video_duration_raw - 2

        # 提取封面
        script = soup.find_all('script', attrs={'type': 'application/ld+json'})[0]
        json_data = script.string.strip()

        # 提取标题
        title_raw = soup.find("title").text
        title = re.sub(r"_哔哩哔哩_bilibili", "", title_raw).strip()

        # 提取标签
        keywords_content = soup.find("meta", itemprop="keywords")["content"]
        content_without_title = keywords_content.replace(title + ',', '')
        keywords_list = content_without_title.split(',')
        tags = ",".join(keywords_list[:-4])

        meta_description = soup.find("meta", itemprop="description")["content"]
        numbers = re.findall(
            r'[\s\S]*?视频播放量 (\d+)、弹幕量 (\d+)、点赞数 (\d+)、投硬币枚数 (\d+)、收藏人数 (\d+)、转发人数 (\d+)',
            meta_description)

        # 提取作者
        author_search = re.search(r"视频作者\s*([^,]+)", meta_description)
        if author_search:
            author = author_search.group(1).strip()
        else:
            author = "NA"

        # 提取视频简介
        meta_parts = re.split(r',\s*', meta_description)
        if meta_parts:
            video_desc = meta_parts[0].strip()
        else:
            video_desc = "NA"

        if numbers:
            views, danmaku, likes, coins, favorites, shares = [int(n) for n in numbers[0]]
            data.img = json.loads(json_data)['thumbnailUrl'][0]
            data.title = title
            data.author = author
            data.desc = video_desc
            data.watch = views
            data.barrage = danmaku
            data.like = likes
            data.coin = coins
            data.collection = favorites
            data.share = shares
            data.avUrl = get_video_url(str(video_aid))
            data.bvUrl = get_video_url(str(video_bvid))
            print(json.dumps(data.__json__(), ensure_ascii=False))
        else:
            raise ValueError("未找到相关数据，可能为分集视频")

    except Exception as e:
        print(f'{"error": "{e}"}')
