package com.ruoyi.hisexam.service;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.hisexam.domain.ExamApply;
import com.ruoyi.hisexam.domain.ExamResult;

/**
 * Builds real-time PDF reports from saved examination results.
 */
@Service
public class ExamReportPdfService
{
    @Value("${file.path:D:/RuoYi-Code/Hospital/RuoYi-Cloud/ruoyi/uploadPath}")
    private String uploadBasePath;

    @Value("${file.prefix:/profile}")
    private String filePrefix;

    public byte[] buildReport(ExamApply apply, List<ExamResult> results)
    {
        if (apply == null || apply.getApplyId() == null)
        {
            throw new ServiceException("检查申请单不存在");
        }
        ExamResult result = selectPrimaryResult(results);
        if (result == null)
        {
            throw new ServiceException("请先保存诊断结果后再查看PDF报告");
        }

        boolean hasInspection = hasInspectionResults(results);
        boolean hasImage = hasImageResult(results);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
        {
            Document document = new Document(PageSize.A4, 42, 42, 36, 36);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = font(22, Font.BOLD);
            Font subTitleFont = font(15, Font.BOLD);
            Font labelFont = font(10, Font.BOLD);
            Font bodyFont = font(10, Font.NORMAL);
            Font mutedFont = font(9, Font.NORMAL);

            Paragraph hospital = new Paragraph("云 医 智 联 门 诊", titleFont);
            hospital.setAlignment(Element.ALIGN_CENTER);
            hospital.setSpacingAfter(6);
            document.add(hospital);

            Paragraph title = new Paragraph("检查报告单", subTitleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(16);
            document.add(title);

            document.add(buildMetaTable(apply, result, labelFont, bodyFont));
            document.add(sectionTitle("内容详情", labelFont));
            if (hasInspection)
            {
                document.add(buildInspectionTable(results, labelFont, bodyFont));
            }
            if (hasImage)
            {
                addImage(document, result.getImageUrl(), mutedFont);
            }
            if (hasImage)
            {
                document.add(section("检查所见", value(result.getImageFind(), ""), labelFont, bodyFont));
            }
            document.add(section("诊断意见", buildOpinion(result), labelFont, bodyFont));

            Paragraph footer = new Paragraph("本报告仅供临床参考，请结合患者实际情况综合判断。", mutedFont);
            footer.setSpacingBefore(18);
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            document.close();
            return outputStream.toByteArray();
        }
        catch (Exception e)
        {
            throw new ServiceException("生成PDF报告失败：" + e.getMessage());
        }
    }

    private PdfPTable buildMetaTable(ExamApply apply, ExamResult result, Font labelFont, Font bodyFont)
    {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingAfter(14);
        try
        {
            table.setWidths(new float[] { 1.1f, 1.6f, 1.1f, 1.6f });
        }
        catch (Exception ignored)
        {
        }

        addCell(table, "检查号", labelFont, true);
        addCell(table, value(result.getResultId(), apply.getApplyId()), bodyFont, false);
        addCell(table, "病人ID", labelFont, true);
        addCell(table, value(apply.getPatientId(), ""), bodyFont, false);
        addCell(table, "姓名", labelFont, true);
        addCell(table, value(apply.getPatientName(), ""), bodyFont, false);
        addCell(table, "性别/年龄", labelFont, true);
        addCell(table, formatGender(apply.getGender()) + " / " + (apply.getAge() == null ? "" : apply.getAge() + "岁"), bodyFont, false);
        addCell(table, "检查项目", labelFont, true);
        addCell(table, value(apply.getTechName(), apply.getApplyPosition()), bodyFont, false);
        addCell(table, "检查时间", labelFont, true);
        addCell(table, formatDate(firstDate(result.getReportTime(), apply.getExamTime(), apply.getApplyTime())), bodyFont, false);
        addCell(table, "申请科室", labelFont, true);
        addCell(table, value(apply.getDeptName(), ""), bodyFont, false);
        addCell(table, "申请医生", labelFont, true);
        addCell(table, value(apply.getDoctorName(), ""), bodyFont, false);
        return table;
    }

    private void addCell(PdfPTable table, String text, Font font, boolean label)
    {
        PdfPCell cell = new PdfPCell(new Phrase(StringUtils.defaultString(text), font));
        cell.setPadding(8);
        cell.setMinimumHeight(28);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(new Color(210, 216, 224));
        if (label)
        {
            cell.setBackgroundColor(new Color(244, 247, 251));
        }
        table.addCell(cell);
    }

    private Paragraph sectionTitle(String title, Font font)
    {
        Paragraph paragraph = new Paragraph(title, font);
        paragraph.setSpacingBefore(8);
        paragraph.setSpacingAfter(8);
        return paragraph;
    }

    private PdfPTable section(String title, String content, Font labelFont, Font bodyFont)
    {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(8);
        table.setSpacingAfter(4);

        PdfPCell titleCell = new PdfPCell(new Phrase(title, labelFont));
        titleCell.setPadding(8);
        titleCell.setBorderColor(new Color(210, 216, 224));
        titleCell.setBackgroundColor(new Color(244, 247, 251));
        table.addCell(titleCell);

        PdfPCell bodyCell = new PdfPCell(new Phrase(StringUtils.defaultIfBlank(content, "暂无"), bodyFont));
        bodyCell.setPadding(10);
        bodyCell.setMinimumHeight(64);
        bodyCell.setLeading(4, 1.3f);
        bodyCell.setBorderColor(new Color(210, 216, 224));
        table.addCell(bodyCell);
        return table;
    }

    private PdfPTable buildInspectionTable(List<ExamResult> results, Font labelFont, Font bodyFont)
    {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingAfter(10);
        try
        {
            table.setWidths(new float[] { 2.2f, 1.2f, 1.0f, 1.8f, 1.2f });
        }
        catch (Exception ignored)
        {
        }

        addInspectionCell(table, "项目名称", labelFont, true, false);
        addInspectionCell(table, "结果", labelFont, true, false);
        addInspectionCell(table, "单位", labelFont, true, false);
        addInspectionCell(table, "参考范围", labelFont, true, false);
        addInspectionCell(table, "异常标识", labelFont, true, false);

        boolean hasRows = false;
        for (ExamResult result : results)
        {
            if (!isInspectionResult(result))
            {
                continue;
            }
            hasRows = true;
            boolean abnormal = isAbnormal(result.getAbnormalFlag());
            addInspectionCell(table, value(result.getItemName(), result.getItemCode()), bodyFont, false, false);
            addInspectionCell(table, value(result.getResultValue(), ""), bodyFont, false, abnormal);
            addInspectionCell(table, value(result.getResultUnit(), ""), bodyFont, false, false);
            addInspectionCell(table, value(result.getReferenceRange(), ""), bodyFont, false, false);
            addInspectionCell(table, formatAbnormalFlag(result.getAbnormalFlag()), bodyFont, false, abnormal);
        }
        if (!hasRows)
        {
            addInspectionCell(table, "暂无检验明细", bodyFont, false, false);
            addInspectionCell(table, "", bodyFont, false, false);
            addInspectionCell(table, "", bodyFont, false, false);
            addInspectionCell(table, "", bodyFont, false, false);
            addInspectionCell(table, "", bodyFont, false, false);
        }
        return table;
    }

    private void addInspectionCell(PdfPTable table, String text, Font font, boolean header, boolean abnormal)
    {
        Font cellFont = abnormal ? new Font(font.getBaseFont(), font.getSize(), Font.BOLD, new Color(190, 18, 60)) : font;
        PdfPCell cell = new PdfPCell(new Phrase(StringUtils.defaultString(text), cellFont));
        cell.setPadding(7);
        cell.setMinimumHeight(26);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(new Color(210, 216, 224));
        if (header)
        {
            cell.setBackgroundColor(new Color(244, 247, 251));
        }
        table.addCell(cell);
    }

    private void addImage(Document document, String imageUrl, Font mutedFont) throws Exception
    {
        byte[] imageBytes = readImageBytes(imageUrl);
        if (imageBytes == null || imageBytes.length == 0)
        {
            Paragraph empty = new Paragraph("暂无检查图片", mutedFont);
            empty.setSpacingAfter(8);
            document.add(empty);
            return;
        }
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (bufferedImage == null)
        {
            document.add(new Paragraph("检查图片格式无法识别", mutedFont));
            return;
        }

        Image image = Image.getInstance(imageBytes);
        image.scaleToFit(500, 300);
        image.setAlignment(Image.ALIGN_CENTER);
        image.setBorder(Rectangle.BOX);
        image.setBorderWidth(0.5f);
        image.setBorderColor(new Color(210, 216, 224));
        image.setSpacingAfter(10);
        document.add(image);
    }

    private ExamResult selectPrimaryResult(List<ExamResult> results)
    {
        if (results == null || results.isEmpty())
        {
            return null;
        }
        for (ExamResult result : results)
        {
            if (result != null && Long.valueOf(1L).equals(result.getSort()))
            {
                return result;
            }
        }
        return results.get(0);
    }

    private boolean hasInspectionResults(List<ExamResult> results)
    {
        if (results == null)
        {
            return false;
        }
        for (ExamResult result : results)
        {
            if (isInspectionResult(result))
            {
                return true;
            }
        }
        return false;
    }

    private boolean hasImageResult(List<ExamResult> results)
    {
        if (results == null)
        {
            return false;
        }
        for (ExamResult result : results)
        {
            if (result != null && StringUtils.isNotBlank(result.getImageUrl()))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isInspectionResult(ExamResult result)
    {
        if (result == null)
        {
            return false;
        }
        return "INSPEC".equals(result.getResultType())
                || "LAB".equals(result.getResultType())
                || "ITEM".equals(result.getItemType());
    }

    private boolean isAbnormal(String abnormalFlag)
    {
        return StringUtils.isNotBlank(abnormalFlag) && !"0".equals(abnormalFlag) && !"4".equals(abnormalFlag);
    }

    private byte[] readImageBytes(String imageUrl) throws Exception
    {
        if (StringUtils.isBlank(imageUrl))
        {
            return null;
        }
        if (imageUrl.startsWith("data:image"))
        {
            String base64 = StringUtils.substringAfter(imageUrl, ",");
            return Base64.getDecoder().decode(base64.getBytes(StandardCharsets.UTF_8));
        }

        File localFile = resolveLocalFile(imageUrl);
        if (localFile != null && localFile.exists() && localFile.isFile())
        {
            return readAll(new FileInputStream(localFile));
        }
        return readAll(new URL(imageUrl).openStream());
    }

    private byte[] readAll(InputStream inputStream) throws Exception
    {
        try (InputStream in = inputStream; ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
        {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = in.read(buffer)) != -1)
            {
                outputStream.write(buffer, 0, length);
            }
            return outputStream.toByteArray();
        }
    }

    private File resolveLocalFile(String imageUrl)
    {
        String normalized = imageUrl.replace("\\", "/");
        String relative = null;
        if (StringUtils.isNotBlank(filePrefix) && normalized.contains(filePrefix))
        {
            relative = StringUtils.substringAfter(normalized, filePrefix);
        }
        else if (normalized.startsWith("/"))
        {
            relative = normalized;
        }
        if (StringUtils.isBlank(relative) || relative.contains(".."))
        {
            return null;
        }
        while (relative.startsWith("/") || relative.startsWith("\\"))
        {
            relative = relative.substring(1);
        }
        return new File(uploadBasePath, relative);
    }

    private Font font(float size, int style) throws Exception
    {
        String fontPath = resolveChineseFont();
        BaseFont baseFont = "STSong-Light".equals(fontPath)
                ? BaseFont.createFont(fontPath, "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED)
                : BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        return new Font(baseFont, size, style, Color.BLACK);
    }

    private String resolveChineseFont()
    {
        String[] candidates = new String[] {
                "C:/Windows/Fonts/simsun.ttc,0",
                "C:/Windows/Fonts/msyh.ttc,0",
                "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc,0",
                "/usr/share/fonts/truetype/wqy/wqy-microhei.ttc"
        };
        for (String candidate : candidates)
        {
            String path = candidate.contains(",") ? candidate.substring(0, candidate.indexOf(",")) : candidate;
            if (new File(path).exists())
            {
                return candidate;
            }
        }
        return "STSong-Light";
    }

    private String buildOpinion(ExamResult result)
    {
        StringBuilder builder = new StringBuilder();
        appendLine(builder, result.getDiagnosisOpinion());
        appendLine(builder, result.getDiagnosisResult());
        appendLine(builder, result.getSuggestion());
        appendLine(builder, result.getRemark());
        return builder.length() == 0 ? "" : builder.toString();
    }

    private void appendLine(StringBuilder builder, String value)
    {
        if (StringUtils.isBlank(value))
        {
            return;
        }
        if (builder.length() > 0)
        {
            builder.append("\n");
        }
        builder.append(value);
    }

    private String formatAbnormalFlag(String abnormalFlag)
    {
        if ("0".equals(abnormalFlag))
        {
            return "正常";
        }
        if ("1".equals(abnormalFlag))
        {
            return "偏高";
        }
        if ("2".equals(abnormalFlag))
        {
            return "偏低";
        }
        if ("3".equals(abnormalFlag))
        {
            return "阳性";
        }
        if ("4".equals(abnormalFlag))
        {
            return "阴性";
        }
        return value(abnormalFlag, "");
    }

    private Date firstDate(Date... dates)
    {
        for (Date date : dates)
        {
            if (date != null)
            {
                return date;
            }
        }
        return new Date();
    }

    private String formatDate(Date date)
    {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    private String formatGender(String gender)
    {
        if ("0".equals(gender))
        {
            return "男";
        }
        if ("1".equals(gender) || "2".equals(gender))
        {
            return "女";
        }
        return value(gender, "");
    }

    private String value(Object primary, Object fallback)
    {
        if (primary != null && StringUtils.isNotBlank(String.valueOf(primary)))
        {
            return String.valueOf(primary);
        }
        return fallback == null ? "" : String.valueOf(fallback);
    }
}
