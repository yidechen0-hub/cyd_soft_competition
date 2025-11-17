import requests

def reverse_geocode_osm(latitude: float, longitude: float) -> str:
    """
    使用 OpenStreetMap 逆地理编码（免费、无密钥）
    :param latitude: 纬度（北纬为正，南纬为负）
    :param longitude: 经度（东经为正，西经为负）
    :return: 结构化地址字符串
    """
    url = f"https://nominatim.openstreetmap.org/reverse?lat={latitude}&lon={longitude}&format=json&accept-language=zh-CN"
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }  # 必须设置 User-Agent，否则会被拒绝访问

    try:
        response = requests.get(url, headers=headers, timeout=10)
        response.raise_for_status()  # 抛出 HTTP 错误
        data = response.json()

        # 解析地址（优先取详细地址，无则拼接关键信息）
        address = data.get("display_name", "未查询到地址")
        return address

    except Exception as e:
        return f"查询失败：{str(e)}"

# 测试：北纬21.306944，西经157.858333（西经转换为负数：-157.858333）
if __name__ == "__main__":
    lat = 21.306944  # 北纬
    lon = -157.858333  # 西经（转换为负数）
    address = reverse_geocode_osm(lat, lon)
    print(f"经纬度：{lat}, {lon}")
    print(f"对应地址：{address}")