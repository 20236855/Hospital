"""
医院信息管理系统(HIS) - 按角色独立用例图（详细版）
每个角色生成一张独立的用例图
"""

from PIL import Image, ImageDraw, ImageFont
import os
import math

def draw_actor(draw, x, y, name, font):
    draw.ellipse([x-60, y-68, x+60, y+24], outline='#2d3436', fill='#f5f6fa', width=4)
    draw.line([x, y+24, x, y+125], fill='#2d3436', width=5)
    draw.line([x, y+42, x-42, y+85], fill='#2d3436', width=5)
    draw.line([x, y+42, x+42, y+85], fill='#2d3436', width=5)
    draw.line([x, y+125, x-42, y+168], fill='#2d3436', width=5)
    draw.line([x, y+125, x+42, y+168], fill='#2d3436', width=5)
    bbox = draw.textbbox((0, 0), name, font=font)
    draw.text((x - (bbox[2]-bbox[0])//2, y + 190), name, font=font, fill='#2d3436')

def draw_use_case(draw, x, y, text, font, uc_width, uc_height):
    draw.ellipse([x - uc_width//2, y - uc_height//2, x + uc_width//2, y + uc_height//2],
                fill='white', outline='#495057', width=3)
    bbox = draw.textbbox((0, 0), text, font=font)
    draw.text((x - (bbox[2]-bbox[0])//2, y - 30), text, font=font, fill='#2d3436')
    return (x, y)

def draw_line(draw, x1, y1, x2, y2, color='#495057', width=3):
    draw.line([(x1, y1), (x2, y2)], fill=color, width=width)

def draw_single_role_diagram(role_id, role_name, role_ucs, module_name, output_dir):
    # 字体
    try:
        font_title = ImageFont.truetype("simhei.ttf", 120)
        font_actor = ImageFont.truetype("simhei.ttf", 84)
        font_module = ImageFont.truetype("simhei.ttf", 72)
        font_uc = ImageFont.truetype("simhei.ttf", 60)
    except:
        font_title = ImageFont.load_default()
        font_actor = ImageFont.load_default()
        font_module = ImageFont.load_default()
        font_uc = ImageFont.load_default()

    uc_width = 480
    uc_height = 150
    uc_spacing_y = 250
    uc_spacing_x = 560

    n = len(role_ucs)
    # 根据用例数量计算画布高度和列数（最多2列）
    if n <= 5:
        cols = 1
        rows = n
    else:
        cols = 2
        rows = (n + 1) // 2

    height = max(2600, 900 + rows * uc_spacing_y + 300)
    width = 3800

    img = Image.new('RGB', (width, height), 'white')
    draw = ImageDraw.Draw(img)

    # 标题
    title = f"医院信息管理系统(HIS) - {role_name}用例图"
    bbox = draw.textbbox((0, 0), title, font=font_title)
    draw.text((width//2 - (bbox[2]-bbox[0])//2, 30), title, font=font_title, fill='#1a1a1a')
    draw.line([(200, 160), (width-200, 160)], fill='#333333', width=5)

    # 系统边界
    boundary_x = 450
    boundary_y = 230
    boundary_w = width - 2 * boundary_x
    boundary_h = height - 650
    boundary_right = boundary_x + boundary_w
    draw.rectangle([boundary_x, boundary_y, boundary_right, boundary_y + boundary_h],
                   fill='#fafafa', outline='#2d3436', width=5)
    draw.text((boundary_x + 40, boundary_y - 50), "医院信息管理系统(HIS)", font=font_module, fill='#2d3436')

    # 模块名称标注
    module_label = module_name
    bbox_mod = draw.textbbox((0, 0), module_label, font=font_module)
    module_label_x = width // 2 - (bbox_mod[2] - bbox_mod[0]) // 2
    draw.text((module_label_x, boundary_y + 50), module_label, font=font_module, fill='#4a4a4a')

    # 参与者小人（在边界框外左侧）
    actor_x = 200
    actor_y = height // 2 - 150
    draw_actor(draw, actor_x, actor_y, role_name, font_actor)

    # 用例椭圆 - 分多列布局，居中分布在边界框内
    center_x = (boundary_x + boundary_right) // 2
    use_case_pos = {}

    if cols == 1:
        # 单列居中
        uc_x = center_x
        start_y = boundary_y + 200
        for i, (name, _, _) in enumerate(role_ucs):
            y = start_y + i * uc_spacing_y
            use_case_pos[name] = draw_use_case(draw, uc_x, y, name, font_uc, uc_width, uc_height)
    else:
        # 双列分布
        col1_x = center_x - uc_spacing_x // 2 - 40
        col2_x = center_x + uc_spacing_x // 2 + 40
        start_y = boundary_y + 200

        # 第一列
        for i in range(rows):
            if i < len(role_ucs):
                name = role_ucs[i][0]
                y = start_y + i * uc_spacing_y
                use_case_pos[name] = draw_use_case(draw, col1_x, y, name, font_uc, uc_width, uc_height)

        # 第二列
        for i in range(rows):
            idx = rows + i
            if idx < len(role_ucs):
                name = role_ucs[idx][0]
                y = start_y + i * uc_spacing_y
                use_case_pos[name] = draw_use_case(draw, col2_x, y, name, font_uc, uc_width, uc_height)

    # 关联关系 - 参与者连到自己的用例（每条线从角色头部右侧发散到椭圆左侧）
    for i, (name, _, _) in enumerate(role_ucs):
        if name in use_case_pos:
            pos = use_case_pos[name]
            # 连线：从角色右侧到椭圆左侧，带轻微偏移避免完全重叠
            y_offset = (i - (n - 1) / 2) * 15
            draw_line(draw, actor_x + 60, actor_y + 50 + y_offset, pos[0] - uc_width//2, pos[1])

    output_path = os.path.join(output_dir, f"{role_name}_用例图.png")
    img.save(output_path, 'PNG')
    print(f"✅ {role_name} 用例图已生成：{output_path}")
    return output_path

def generate_all_role_diagrams(output_dir):
    # 定义每个角色的用例
    roles_data = [
        {
            "id": "patient",
            "name": "患者",
            "module": "患者服务",
            "ucs": [
                ("在线注册账号", "", ""),
                ("在线预约挂号", "", ""),
                ("在线支付费用", "", ""),
                ("签到候诊", "", ""),
                ("查看电子病历", "", ""),
                ("查看检查报告", "", ""),
                ("查看检验报告", "", ""),
                ("申请退号退费", "", ""),
            ]
        },
        {
            "id": "doctor",
            "name": "医生",
            "module": "医生诊疗",
            "ucs": [
                ("接诊患者", "", ""),
                ("书写电子病历", "", ""),
                ("开具检查申请", "", ""),
                ("开具检验申请", "", ""),
                ("开具电子处方", "", ""),
                ("查看排班信息", "", ""),
            ]
        },
        {
            "id": "reg_worker",
            "name": "挂号工作者",
            "module": "挂号管理",
            "ucs": [
                ("线下窗口挂号", "", ""),
                ("现金收费结算", "", ""),
                ("处理退号申请", "", ""),
                ("管理号源信息", "", ""),
            ]
        },
        {
            "id": "exam_doctor",
            "name": "检查医生",
            "module": "检查管理",
            "ucs": [
                ("执行检查项目", "", ""),
                ("生成检查报告", "", ""),
            ]
        },
        {
            "id": "lab_doctor",
            "name": "检验医生",
            "module": "检验管理",
            "ucs": [
                ("执行检验项目", "", ""),
                ("生成检验报告", "", ""),
            ]
        },
        {
            "id": "pharmacy_admin",
            "name": "药房管理员",
            "module": "药房管理",
            "ucs": [
                ("审核电子处方", "", ""),
                ("调配药品", "", ""),
                ("发放药品", "", ""),
                ("药品库存管理", "", ""),
            ]
        },
        {
            "id": "admin",
            "name": "系统管理员",
            "module": "系统管理",
            "ucs": [
                ("管理用户账号", "", ""),
                ("管理角色权限", "", ""),
                ("系统配置管理", "", ""),
                ("数据统计分析", "", ""),
            ]
        },
    ]

    generated_files = []
    for role in roles_data:
        path = draw_single_role_diagram(role["id"], role["name"], role["ucs"], role["module"], output_dir)
        generated_files.append(path)

    print(f"\n🎉 全部 7 张角色用例图生成完成！")
    return generated_files


if __name__ == '__main__':
    output_dir = os.path.dirname(os.path.abspath(__file__))
    generate_all_role_diagrams(output_dir)
