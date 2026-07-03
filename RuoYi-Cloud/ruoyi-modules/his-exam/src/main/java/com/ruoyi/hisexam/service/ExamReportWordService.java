package com.ruoyi.hisexam.service;

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
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlCursor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.hisexam.domain.ExamApply;
import com.ruoyi.hisexam.domain.ExamResult;

/**
 * Builds Word reports for image-based examination results.
 */
@Service
public class ExamReportWordService
{
    @Value("${exam.report.template-path:D:/RuoYi-Code/Hospital/云 医 智 联 门 诊.docx}")
    private String templatePath;

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
            throw new ServiceException("请先保存诊断结果后再下载Word报告");
        }

        File template = new File(templatePath);
        if (!template.exists())
        {
            throw new ServiceException("Word报告模板不存在：" + templatePath);
        }

        try (InputStream inputStream = new FileInputStream(template);
             XWPFDocument document = new XWPFDocument(inputStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
        {
            fillDocument(document, apply, results);
            document.write(outputStream);
            return outputStream.toByteArray();
        }
        catch (Exception e)
        {
            throw new ServiceException("生成Word报告失败：" + e.getMessage());
        }
    }

    private void fillDocument(XWPFDocument document, ExamApply apply, List<ExamResult> results) throws Exception
    {
        ExamResult result = selectPrimaryResult(results);
        boolean hasInspection = hasInspectionResults(results);
        boolean hasImage = hasImageResult(results);
        String examNo = value(result.getResultId(), apply.getApplyId());
        String patientId = value(apply.getPatientId(), "");
        String patientName = value(apply.getPatientName(), "");
        String gender = formatGender(apply.getGender());
        String age = apply.getAge() == null ? "" : apply.getAge() + "岁";
        String techName = value(apply.getTechName(), apply.getApplyPosition());
        String reportTime = formatDate(firstDate(result.getReportTime(), apply.getExamTime(), apply.getApplyTime()));
        String deptName = value(apply.getDeptName(), "");
        String doctorName = value(apply.getDoctorName(), "");
        String findings = value(result.getImageFind(), "");
        String opinion = buildOpinion(result);

        String examLine = "检查号：" + examNo + "                                      病人ID：" + patientId;
        String patientLine = "姓 名 ：" + patientName + "        性别：" + gender + "        年龄：" + age;
        String itemLine = "检查项目：" + techName + "                                  检查时间：" + reportTime;
        String applyLine = "申请科室：" + deptName + "                                  申请医生：" + doctorName;

        replaceParagraphStartsWith(document.getParagraphs(), "检查号：", examLine);
        replaceParagraphStartsWith(document.getParagraphs(), "姓 名", patientLine);
        replaceParagraphStartsWith(document.getParagraphs(), "检查项目：", itemLine);
        replaceParagraphStartsWith(document.getParagraphs(), "申请科室：", applyLine);
        replaceParagraphStartsWith(document.getParagraphs(), "检查所见：", "检查所见：" + findings);
        replaceParagraphStartsWith(document.getParagraphs(), "诊断意见：", "诊断意见：" + opinion);

        List<XWPFTable> tables = document.getTables();
        if (!tables.isEmpty())
        {
            XWPFTable table = tables.get(0);
            setCellText(table, 0, 0, examLine);
            setCellText(table, 1, 0, patientLine);
            setCellText(table, 2, 0, itemLine + "\n" + applyLine);
        }
        if (tables.size() > 1)
        {
            XWPFTable detailTable = tables.get(1);
            XWPFTableCell detailCell = detailTable.getRow(0).getCell(0);
            if (hasInspection)
            {
                fillInspectionCell(detailCell, results);
                setCellText(detailTable, 1, 0, "诊断意见：" + opinion);
            }
            else if (hasImage)
            {
                fillImageCell(detailCell, result.getImageUrl());
                setCellText(detailTable, 1, 0, "检查所见：" + findings);
            }
            else
            {
                setCellParagraph(detailCell, "内容详情：暂无检查图片或检验明细");
                setCellText(detailTable, 1, 0, "诊断意见：" + opinion);
            }
        }
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

    private void replaceParagraphStartsWith(List<XWPFParagraph> paragraphs, String prefix, String value)
    {
        for (XWPFParagraph paragraph : paragraphs)
        {
            String text = paragraph.getText();
            if (text != null && text.trim().startsWith(prefix))
            {
                setParagraphText(paragraph, value);
            }
        }
    }

    private void setCellText(XWPFTable table, int rowIndex, int cellIndex, String text)
    {
        if (table.getRow(rowIndex) == null || table.getRow(rowIndex).getCell(cellIndex) == null)
        {
            return;
        }
        XWPFTableCell cell = table.getRow(rowIndex).getCell(cellIndex);
        if (cell.getParagraphs().isEmpty())
        {
            cell.addParagraph();
        }
        setParagraphText(cell.getParagraphs().get(0), text);
    }

    private void setParagraphText(XWPFParagraph paragraph, String text)
    {
        for (int i = paragraph.getRuns().size() - 1; i >= 0; i--)
        {
            paragraph.removeRun(i);
        }
        String[] lines = StringUtils.defaultString(text).split("\\n", -1);
        XWPFRun run = paragraph.createRun();
        for (int i = 0; i < lines.length; i++)
        {
            if (i > 0)
            {
                run.addBreak();
            }
            run.setText(lines[i]);
        }
    }

    private void fillImageCell(XWPFTableCell cell, String imageUrl)
    {
        try
        {
            byte[] imageBytes = readImageBytes(imageUrl);
            if (imageBytes == null || imageBytes.length == 0)
            {
                setCellParagraph(cell, "内容详情：暂无检查图片");
                return;
            }
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null)
            {
                setCellParagraph(cell, "内容详情：检查图片格式无法识别");
                return;
            }

            XWPFParagraph paragraph = cell.getParagraphs().isEmpty() ? cell.addParagraph() : cell.getParagraphs().get(0);
            setParagraphText(paragraph, "内容详情：");
            XWPFRun run = paragraph.createRun();
            run.addBreak();
            int width = Math.min(430, image.getWidth());
            int height = Math.max(120, (int) Math.round((double) image.getHeight() * width / image.getWidth()));
            run.addPicture(new ByteArrayInputStream(imageBytes), pictureType(imageUrl), "exam-image", Units.toEMU(width), Units.toEMU(height));
        }
        catch (Exception e)
        {
            setCellParagraph(cell, "内容详情：检查图片读取失败");
        }
    }

    private void fillInspectionCell(XWPFTableCell cell, List<ExamResult> results)
    {
        XWPFParagraph paragraph = cell.getParagraphs().isEmpty() ? cell.addParagraph() : cell.getParagraphs().get(0);
        setParagraphText(paragraph, "内容详情：检验结果");

        XmlCursor cursor = paragraph.getCTP().newCursor();
        cursor.toEndToken();
        XWPFTable table = cell.insertNewTbl(cursor);
        setRowText(table.getRow(0), new String[] { "项目名称", "结果", "单位", "参考范围", "异常标识" });

        boolean hasRows = false;
        for (ExamResult result : results)
        {
            if (!isInspectionResult(result))
            {
                continue;
            }
            hasRows = true;
            XWPFTableRow row = table.createRow();
            setRowText(row, new String[] {
                    value(result.getItemName(), result.getItemCode()),
                    value(result.getResultValue(), ""),
                    value(result.getResultUnit(), ""),
                    value(result.getReferenceRange(), ""),
                    formatAbnormalFlag(result.getAbnormalFlag())
            });
        }
        if (!hasRows)
        {
            XWPFTableRow row = table.createRow();
            setRowText(row, new String[] { "暂无检验明细", "", "", "", "" });
        }
    }

    private void setRowText(XWPFTableRow row, String[] values)
    {
        if (row == null)
        {
            return;
        }
        while (row.getTableCells().size() < values.length)
        {
            row.addNewTableCell();
        }
        for (int i = 0; i < values.length; i++)
        {
            XWPFTableCell cell = row.getCell(i);
            if (cell.getParagraphs().isEmpty())
            {
                cell.addParagraph();
            }
            setParagraphText(cell.getParagraphs().get(0), values[i]);
        }
    }

    private void setCellParagraph(XWPFTableCell cell, String text)
    {
        XWPFParagraph paragraph = cell.getParagraphs().isEmpty() ? cell.addParagraph() : cell.getParagraphs().get(0);
        setParagraphText(paragraph, text);
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
            try (InputStream inputStream = new FileInputStream(localFile);
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
            {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = inputStream.read(buffer)) != -1)
                {
                    outputStream.write(buffer, 0, length);
                }
                return outputStream.toByteArray();
            }
        }

        try (InputStream inputStream = new URL(imageUrl).openStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
        {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) != -1)
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

    private int pictureType(String imageUrl)
    {
        String url = StringUtils.defaultString(imageUrl).toLowerCase();
        if (url.contains("jpg") || url.contains("jpeg"))
        {
            return Document.PICTURE_TYPE_JPEG;
        }
        return Document.PICTURE_TYPE_PNG;
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
