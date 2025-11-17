import exifread
import os
import math
from datetime import datetime

def get_geo(photo_path):
    print(f"正在读取照片：{photo_path}")
    """读取照片 EXIF 中的 GPS 信息，返回 (纬度, 经度) 十进制元组，若无则返回 None"""
    if not os.path.exists(photo_path) or not os.path.isfile(photo_path):
        print(f"❌ 文件不存在或不是有效文件：{photo_path}")
        return None

    try:
        with open(photo_path, 'rb') as f:
            tags = exifread.process_file(f, details=False)  # 可设为 False 加快速度

        # 直接解析 GPS 并返回结果
        return parse_exif_gps(tags)

    except Exception as e:
        print(f"❌ 读取失败：{str(e)}")
        return None


def parse_exif_gps(tags):
    """解析 GPS 位置信息，返回 (latitude, longitude) 十进制浮点数元组，若无则返回 None"""
    gps_tags = [tag for tag in tags.keys() if tag.startswith('GPS')]
    if not gps_tags:
        print("📍 照片未包含 GPS 位置数据（可能拍摄时未开启定位）")
        return None

    try:
        # --- 纬度 ---
        lat_deg = tags.get('GPS GPSLatitude')
        lat_ref = tags.get('GPS GPSLatitudeRef')
        if not (lat_deg and lat_ref):
            print("⚠️ 缺少纬度数据")
            return None
        print("lat_deg_val:"+f"{lat_deg.values[0].num}"+"   "+f"{lat_deg.values[0].den}")
        # print("lon_deg_val:"+f"{lon_deg.values[0].num}"+"   "+f"{lon_deg.values[0].den}")
        lat_deg_val = float(lat_deg.values[0].num) / float(lat_deg.values[0].den)
        lat_min_val = float(lat_deg.values[1].num) / float(lat_deg.values[1].den)
        lat_sec_val = float(lat_deg.values[2].num) / float(lat_deg.values[2].den)
        latitude = lat_deg_val + (lat_min_val / 60.0) + (lat_sec_val / 3600.0)
        if str(lat_ref).strip().upper() == 'S':
            latitude = -latitude

        # --- 经度 ---
        lon_deg = tags.get('GPS GPSLongitude')
        lon_ref = tags.get('GPS GPSLongitudeRef')
        if not (lon_deg and lon_ref):
            print("⚠️ 缺少经度数据")
            return None

        lon_deg_val = float(lon_deg.values[0].num) / float(lon_deg.values[0].den)
        lon_min_val = float(lon_deg.values[1].num) / float(lon_deg.values[1].den)
        lon_sec_val = float(lon_deg.values[2].num) / float(lon_deg.values[2].den)
        longitude = lon_deg_val + (lon_min_val / 60.0) + (lon_sec_val / 3600.0)
        if str(lon_ref).strip().upper() == 'W':
            longitude = -longitude

        # 打印友好信息（可选）
        lat_dir = "北纬" if latitude >= 0 else "南纬"
        lon_dir = "东经" if longitude >= 0 else "西经"
        print(f"📍 GPS 位置：{lat_dir} {abs(latitude):.6f}°, {lon_dir} {abs(longitude):.6f}°")
        print(f"   高德地图：https://uri.amap.com/marker?position={abs(longitude):.6f},{abs(latitude):.6f}")
        print(f"   Google 地图：https://www.google.com/maps/search/?api=1&query={latitude:.6f},{longitude:.6f}")

        return [latitude, longitude]

    except Exception as e:
        print(f"⚠️ GPS 解析异常：{e}")
        return None


# 示例使用方式
if __name__ == "__main__":
    photo = "./IMG_9397.JPG"  # 替换为你的照片路径
    coords = get_geo(photo)
    if coords:
        lat, lon = coords
        print(f"\n✅ 返回的经纬度: 纬度={lat}, 经度={lon}")
    else:
        print("\n❌ 未能获取 GPS 信息")