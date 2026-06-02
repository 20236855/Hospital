# 患者病历号自动生成后端修改说明

## 一、Mapper接口修改

在`PatientMapper.java`中新增方法：

```java
import org.apache.ibatis.annotations.Param;

public interface PatientMapper {
    // 其他已有的方法...
    
    /**
     * 查询当天最大病历号
     * @param day 日期前缀（格式：yyyyMMdd）
     * @return 最大病历号
     */
    String selectMaxMedicalIdByDay(@Param("day") String day);
}
```

## 二、Mapper XML修改

在`PatientMapper.xml`中新增SQL查询：

```xml
<select id="selectMaxMedicalIdByDay" resultType="String">
    select max(patient_no) from sys_patient where patient_no like concat(#{day},'%')
</select>
```

## 三、Service层修改

在`PatientServiceImpl.java`的新增方法中添加病历号生成逻辑：

```java
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class PatientServiceImpl implements PatientService {
    
    @Autowired
    private PatientMapper patientMapper;
    
    @Override
    public int insertPatient(Patient patient) {
        // 1.获取当日日期前缀
        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
        
        // 2.查询当天最大病历号
        String maxMedicalId = patientMapper.selectMaxMedicalIdByDay(today);
        String newNo;
        
        if(maxMedicalId == null){
            //当日第一条，从001开始
            newNo = today + "001";
        }else{
            //截取后3位数字+1，不足3位补0
            String numStr = maxMedicalId.substring(8);
            int num = Integer.parseInt(numStr)+1;
            newNo = today + String.format("%03d",num);
        }
        
        //3.自动赋值给实体，前端不用传
        patient.setPatientNo(newNo);
        
        //4.正常insert入库
        return patientMapper.insertPatient(patient);
    }
    
    // 其他方法保持不变...
}
```

## 四、Controller层说明

新增接口不需要修改，保持原有的`@PostMapping("/add")`即可，Service层会自动处理病历号生成。

修改接口不需要修改病历号字段，直接接收前端传递的`patientNo`并更新即可。

## 五、注意事项

1. 确保`sys_patient`表中存在`patient_no`字段
2. 病历号生成规则：yyyyMMdd + 3位自增序号（例：20250603001、20250603002）
3. 新增患者时前端不需要传递`patientNo`，由后端自动生成
4. 修改患者时前端回显原有病历号并禁止修改，后端直接更新原有值