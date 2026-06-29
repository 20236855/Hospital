"""
医院信息管理系统(HIS) - 用例图
参考专业用例图布局：参与者分布在多个边缘，用例成簇分布
"""

from PIL import Image, ImageDraw, ImageFont
import os
import math

def draw_use_case_diagram(output_path):
    width = 6000
    height = 4800
    img = Image.new('RGB', (width, height), 'white')
    draw = ImageDraw.Draw(img)

    try:
        font_title = ImageFont.truetype("simhei.ttf", 160)
        font_actor = ImageFont.truetype("simhei.ttf", 92)
        font_module = ImageFont.truetype("simhei.ttf", 84)
        font_uc = ImageFont.truetype("simhei.ttf", 72)
        font_include = ImageFont.truetype("simsun.ttc", 60)
    except:
        font_title = ImageFont.load_default()
        font_actor = ImageFont.load_default()
        font_module = ImageFont.load_default()
        font_uc = ImageFont.load_default()
        font_include = ImageFont.load_default()

    title = "医院信息管理系统(HIS) - 用例图"
    bbox = draw.textbbox((0, 0), title, font=font_title)
    draw.text((width//2 - (bbox[2]-bbox[0])//2, 40), title, font=font_title, fill='#1a1a1a')
    draw.line([(200, 210), (width-200, 210)], fill='#333333', width=5)

    # ========== 系统边界 ==========
    boundary_x = 500
    boundary_y = 290
    boundary_w = 5000
    boundary_h = 3600
    boundary_right = boundary_x + boundary_w
    draw.rectangle([boundary_x, boundary_y, boundary_right, boundary_y + boundary_h],
                   fill='#fafafa', outline='#2d3436', width=5)
    draw.text((boundary_x + 40, boundary_y - 50), "医院信息管理系统(HIS)", font=font_module, fill='#2d3436')

    # ========== 参与者 - 分布在多个边缘位置 ==========
    actors = [
        {"id": "patient", "name": "患者", "x": 200, "y": 1400},
        {"id": "doctor", "name": "医生", "x": 200, "y": 2000},
        {"id": "reg_worker", "name": "挂号工作者", "x": 200, "y": 2600},
        {"id": "exam_doctor", "name": "检查医生", "x": 5400, "y": 1400},
        {"id": "lab_doctor", "name": "检验医生", "x": 5400, "y": 2000},
        {"id": "pharmacy_admin", "name": "药房管理员", "x": 5400, "y": 2600},
        {"id": "admin", "name": "系统管理员", "x": 2500, "y": 3980},
    ]

    def draw_actor(draw, x, y, name, font):
        draw.ellipse([x-60, y-68, x+60, y+24], outline='#2d3436', fill='#f5f6fa', width=4)
        draw.line([x, y+24, x, y+125], fill='#2d3436', width=5)
        draw.line([x, y+42, x-42, y+85], fill='#2d3436', width=5)
        draw.line([x, y+42, x+42, y+85], fill='#2d3436', width=5)
        draw.line([x, y+125, x-42, y+168], fill='#2d3436', width=5)
        draw.line([x, y+125, x+42, y+168], fill='#2d3436', width=5)
        bbox = draw.textbbox((0, 0), name, font=font)
        draw.text((x - (bbox[2]-bbox[0])//2, y + 190), name, font=font, fill='#2d3436')

    for actor in actors:
        draw_actor(draw, actor["x"], actor["y"], actor["name"], font_actor)

    # ========== 用例椭圆 - 统一尺寸 ==========
    uc_width = 460
    uc_height = 150

    def draw_use_case(draw, x, y, text, font):
        draw.ellipse([x - uc_width//2, y - uc_height//2, x + uc_width//2, y + uc_height//2],
                    fill='white', outline='#495057', width=3)
        bbox = draw.textbbox((0, 0), text, font=font)
        draw.text((x - (bbox[2]-bbox[0])//2, y - 30), text, font=font, fill='#2d3436')
        return (x, y)

    use_case_pos = {}
    use_case_to_actor = {}

    # ========== 用例按模块成簇分布 ==========
    # 椭圆间距230像素，确保不重叠

    # --- 模块1：患者服务 (左上角，患者附近) ---
    m1_x = 1200
    draw.text((m1_x - 140, 380), "患者服务", font=font_module, fill='#2d3436')
    patient_ucs = [
        ("在线注册账号", m1_x, 520),
        ("在线预约挂号", m1_x, 750),
        ("在线支付费用", m1_x, 980),
        ("签到候诊", m1_x, 1210),
        ("查看电子病历", m1_x, 1440),
        ("查看检查报告", m1_x, 1670),
        ("查看检验报告", m1_x, 1900),
        ("申请退号退费", m1_x, 2130),
    ]
    for name, x, y in patient_ucs:
        use_case_pos[name] = draw_use_case(draw, x, y, name, font_uc)
        use_case_to_actor[name] = "patient"

    # --- 模块2：医生诊疗 (左中部，医生附近) ---
    m2_x = 1900
    draw.text((m2_x - 140, 380), "医生诊疗", font=font_module, fill='#2d3436')
    doctor_ucs = [
        ("接诊患者", m2_x, 520),
        ("书写电子病历", m2_x, 750),
        ("开具检查申请", m2_x, 980),
        ("开具检验申请", m2_x, 1210),
        ("开具电子处方", m2_x, 1440),
        ("查看排班信息", m2_x, 1670),
    ]
    for name, x, y in doctor_ucs:
        use_case_pos[name] = draw_use_case(draw, x, y, name, font_uc)
        use_case_to_actor[name] = "doctor"

    # --- 模块3：挂号管理 (左下角，挂号工作者附近) ---
    m3_x = 1200
    draw.text((m3_x - 140, 2520), "挂号管理", font=font_module, fill='#2d3436')
    reg_ucs = [
        ("线下窗口挂号", m3_x, 2670),
        ("现金收费结算", m3_x, 2900),
        ("处理退号申请", m3_x, 3130),
        ("管理号源信息", m3_x, 3360),
    ]
    for name, x, y in reg_ucs:
        use_case_pos[name] = draw_use_case(draw, x, y, name, font_uc)
        use_case_to_actor[name] = "reg_worker"

    # --- 模块4：检查管理 (右上角，检查医生附近) ---
    m4_x = 2500
    draw.text((m4_x - 140, 380), "检查管理", font=font_module, fill='#2d3436')
    exam_ucs = [
        ("执行检查项目", m4_x, 520),
        ("生成检查报告", m4_x, 750),
    ]
    for name, x, y in exam_ucs:
        use_case_pos[name] = draw_use_case(draw, x, y, name, font_uc)
        use_case_to_actor[name] = "exam_doctor"

    # --- 模块5：检验管理 (右上部，检验医生附近) ---
    m5_x = 3100
    draw.text((m5_x - 140, 380), "检验管理", font=font_module, fill='#2d3436')
    lab_ucs = [
        ("执行检验项目", m5_x, 520),
        ("生成检验报告", m5_x, 750),
    ]
    for name, x, y in lab_ucs:
        use_case_pos[name] = draw_use_case(draw, x, y, name, font_uc)
        use_case_to_actor[name] = "lab_doctor"

    # --- 模块6：药房管理 (右下部，药房管理员附近) ---
    m6_x = 3700
    draw.text((m6_x - 140, 1050), "药房管理", font=font_module, fill='#2d3436')
    pharmacy_ucs = [
        ("审核电子处方", m6_x, 1200),
        ("调配药品", m6_x, 1430),
        ("发放药品", m6_x, 1660),
        ("药品库存管理", m6_x, 1890),
    ]
    for name, x, y in pharmacy_ucs:
        use_case_pos[name] = draw_use_case(draw, x, y, name, font_uc)
        use_case_to_actor[name] = "pharmacy_admin"

    # --- 公共用例：验证用户身份 (画布正中间，系统管理之上) ---
    common_x = 2500
    common_y = 2350
    use_case_pos["验证用户身份"] = draw_use_case(draw, common_x, common_y, "验证用户身份", font_uc)

    # --- 模块7：系统管理 (边界内底部，在系统管理员上方) ---
    draw.text((2500 - 140, 3500), "系统管理", font=font_module, fill='#2d3436')
    admin_ucs = [
        ("管理用户账号", 1750, 3650),
        ("管理角色权限", 2250, 3650),
        ("系统配置管理", 2750, 3650),
        ("数据统计分析", 3250, 3650),
    ]
    for name, x, y in admin_ucs:
        use_case_pos[name] = draw_use_case(draw, x, y, name, font_uc)
        use_case_to_actor[name] = "admin"

    # ========== 辅助绘制函数 ==========
    def draw_line(draw, x1, y1, x2, y2, color='#495057', width=3):
        draw.line([(x1, y1), (x2, y2)], fill=color, width=width)

    def draw_dashed_line(draw, x1, y1, x2, y2, color='#6c757d', width=3):
        dx = x2 - x1
        dy = y2 - y1
        length = math.sqrt(dx*dx + dy*dy)
        if length == 0:
            return
        nx = dx / length
        ny = dy / length
        current = 0
        while current < length:
            segment_end = min(current + 12, length)
            sx1 = x1 + nx * current
            sy1 = y1 + ny * current
            sx2 = x1 + nx * segment_end
            sy2 = y1 + ny * segment_end
            draw.line([(sx1, sy1), (sx2, sy2)], fill=color, width=width)
            current += 18

    def draw_arrow(draw, x, y, color='#6c757d'):
        draw.polygon([(x, y), (x-8, y-14), (x+8, y-14)], fill=color)

    # ========== 关联关系 - 每个参与者连到自己的用例 ==========
    actor_pos = {}
    for a in actors:
        actor_pos[a["id"]] = (a["x"], a["y"])

    # 患者 -> 患者服务用例
    px, py = actor_pos["patient"]
    for i, (name, _, _) in enumerate(patient_ucs):
        pos = use_case_pos[name]
        draw_line(draw, px + 38, py + 15 + i*5, pos[0] - uc_width//2, pos[1])

    # 医生 -> 医生诊疗用例
    px, py = actor_pos["doctor"]
    for i, (name, _, _) in enumerate(doctor_ucs):
        pos = use_case_pos[name]
        draw_line(draw, px + 38, py + 15 + i*8, pos[0] - uc_width//2, pos[1])

    # 挂号工作者 -> 挂号管理用例
    px, py = actor_pos["reg_worker"]
    for i, (name, _, _) in enumerate(reg_ucs):
        pos = use_case_pos[name]
        draw_line(draw, px + 38, py + 15 - i*5, pos[0] - uc_width//2, pos[1])

    # 检查医生 -> 检查管理用例
    px, py = actor_pos["exam_doctor"]
    for i, (name, _, _) in enumerate(exam_ucs):
        pos = use_case_pos[name]
        draw_line(draw, px - 38, py + 15 + i*10, pos[0] + uc_width//2, pos[1])

    # 检验医生 -> 检验管理用例
    px, py = actor_pos["lab_doctor"]
    for i, (name, _, _) in enumerate(lab_ucs):
        pos = use_case_pos[name]
        draw_line(draw, px - 38, py + 15 + i*10, pos[0] + uc_width//2, pos[1])

    # 药房管理员 -> 药房管理用例
    px, py = actor_pos["pharmacy_admin"]
    for i, (name, _, _) in enumerate(pharmacy_ucs):
        pos = use_case_pos[name]
        draw_line(draw, px - 38, py + 15 - i*10, pos[0] + uc_width//2, pos[1])

    # 系统管理员 -> 系统管理用例 (从底部向上连)
    px, py = actor_pos["admin"]
    for i, (name, _, _) in enumerate(admin_ucs):
        pos = use_case_pos[name]
        draw_line(draw, px + (i-1.5)*50, py - 68, pos[0], pos[1] + uc_height//2)

    # ========== 包含关系 <<include>> ==========
    # 从各模块第一个用例指向验证用户身份

    include_pairs = [
        patient_ucs[0],
        doctor_ucs[0],
        reg_ucs[0],
        exam_ucs[0],
        lab_ucs[0],
        pharmacy_ucs[0],
        admin_ucs[0],
    ]

    for i, (name, _, _) in enumerate(include_pairs):
        pos = use_case_pos[name]
        if pos[1] < common_y:
            draw_dashed_line(draw, pos[0], pos[1] + uc_height//2 + 5, common_x, common_y - uc_height//2)
            draw_arrow(draw, common_x, common_y - uc_height//2)
        else:
            draw_dashed_line(draw, pos[0], pos[1] - uc_height//2 - 5, common_x, common_y + uc_height//2)
            draw_arrow(draw, common_x, common_y + uc_height//2)
        if i == 0:
            mid_x = (pos[0] + common_x) // 2
            draw.text((mid_x - 80, (pos[1] + common_y)//2), "<<include>>", font=font_include, fill='#6c757d')

    # ========== 图例（已删除） ==========

    img.save(output_path, 'PNG')
    print(f"✅ 专业布局用例图已生成：{output_path}")
    return output_path


if __name__ == '__main__':
    output_path = os.path.join(os.path.dirname(__file__), '医院信息管理系统_用例图.png')
    draw_use_case_diagram(output_path)
