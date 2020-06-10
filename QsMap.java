package com.sgcc.uap.storage.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.beans.BeanMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.sgcc.uap.exception.NestableRuntimeException;
import com.sgcc.uap.rest.utils.DateTimeConverter;

/**
 * 自定义Map集合，以求实Qs开头
 * 
 * @author linbin
 */
public class QsMap extends ConcurrentHashMap<String, Object> implements Comparator<QsMap> {

	private static final long serialVersionUID = 1L;
	
	private final static Logger logger = (Logger) LoggerFactory.getLogger(QsMap.class);

	/**
	 * 构造方法
	 */
	public QsMap() {

	}
	
	/**
	 * 获取对应Key的Stirng值
	 * 
	 * @param key
	 * @return
	 */
	public String getString(String key) {
		Object obj = get(key);
		return obj == null ? "" : obj.toString();
	}

	/**
	 * 获取对应Key的Integer值
	 * 
	 * @param key
	 * @return
	 */
	public int getInt(String key) {
		Object o = get(key);
		if (o != null && !o.equals("")) {
			if (o instanceof Number){
				return ((Number) o).intValue();
			}else {
				return (int) getDouble(key);
			}
		} else {
			return 0;
		}
	}

	/**
	 * 获取对应Key的Double值
	 * 
	 * @param key
	 * @return
	 */
	public double getDouble(String key) {
		Object o = get(key);
		String msg = "QsMap[" + key + "] is not a number. value = [" + o + "]";
		if (o != null) {
			try {
				if (o instanceof Number){
					return ((Number) o).doubleValue();
				}else {
					return Double.parseDouble((String) o);
				}
			} catch (Exception e) {
				logger.error(msg);
				throw new NestableRuntimeException(msg);
			}
		}else {
			logger.error(msg);
			return 0;
		}
	}

	/**
	 * 获取对应Key的boolean值
	 * 
	 * @param key
	 * @return
	 */
	public boolean getBoolean(String key) {
		Object obj = get(key);
		return obj == null ? false : Boolean.valueOf(obj.toString());
	}
	
	/**
	 * 通过json转化为QsMap
	 * 
	 * @param objString
	 * @return
	 */
	public static QsMap fromObject(String objString) {
		if (StringUtil.isEmpty(objString)) {
			return new QsMap();
		} else {
			return JSONObject.parseObject(objString, QsMap.class);
		}
	}
	
	/**
	 * 将object转化为QsMap
	 * @param o
	 * @return
	 */
	public static QsMap fromObject(Object o){
		if (o==null){
			return new QsMap();
		}else {
			return JSONObject.parseObject(JSON.toJSONString(o), QsMap.class);
		}
	}

	/**
	 * 通过key获取QsMap
	 * 
	 * @param key
	 * @return
	 */
	public QsMap getQsMap(String key) {
		Object o = get(key);
		if (StringUtil.isEmpty(key)) {
			return new QsMap();
		} else if (o == null) {
			return new QsMap();
		}
		String type = o.getClass().getName();
		if (this.getClass().getName().equals(type)) {
			return (QsMap) o;
		} else if (new String().getClass().getName().equals(type)) {
			return fromObject(JSON.toJSONString(o));
		} else {
			return fromObject(JSON.toJSONString(o));
		}
	}

	/**
	 * 通过key获取List
	 * 
	 * @param key
	 * @return
	 */
	public List<QsMap> getList(String key) {
		try {
			Object o = get(key);
			if (o==null){
				return new ArrayList<QsMap>();
			}

			//先转化一下o，可能传入的是=，不是:
			Object object = JSON.parse(JSON.toJSONString(o));
			return JSONObject.parseArray(object.toString(), QsMap.class);
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<QsMap>();
		}
	}

	/**
	 * 将QsMap转化为String
	 */
	public String toString() {
		return JSON.toJSONString(this);
	}

	/**
	 * 克隆，效率低但是安全
	 */
	public QsMap clone() {
		QsMap map = new QsMap();
		for (Entry<String, Object> entry: this.entrySet()){
			map.put(entry.getKey(), entry.getValue(), false);
		}
		return map;
	}

	@Override
	public int compare(QsMap o1, QsMap o2) {
		return new Integer(o1.getClass().getName()).compareTo(new Integer(o2.getClass().getName()));
	}
	
	/**
	 * 检查值是否存在
	 */
	public boolean contains(Object value){
		return super.containsValue(value);
	}
	
	/**
	 * 检查键是否存在
	 * @param key
	 * @return
	 */
	public boolean containsKey(String key){
		return super.containsKey(key);
	}
	
	/**
	 * 将Map里的key下划线转小驼峰
	 * @return
	 */
	public QsMap underlineToCamel(){
		QsMap map = new QsMap();
		for (Entry<String, Object> entry: this.entrySet()){
			map.put(underlineToCamel(entry.getKey()), entry.getValue());
		}
		return map;
	}
	
	/**
	 * 将Map里的key小驼峰转下划线
	 * @param isUpper：true：统一都转大写；false-统一转小写
	 * @return
	 */
	public QsMap camelToUnderline(boolean isUpper){
		QsMap map = new QsMap();
		for (Entry<String, Object> entry: this.entrySet()){
			map.put(camelToUnderline(entry.getKey(), isUpper), entry.getValue());
		}
		return map;
	}
	
	/**
	 * 获取map里所有的key(重点注意所有的key都加了单引号)
	 * @param param 传入的map
	 * @param link 连接符，一般是逗号
	 * @return
	 */
	public String getKeys(String link){
		String keys = "";
		for (Entry<String, Object> entry: this.entrySet()){
			keys = keys + "'" + entry.getKey() + "'" + link; 
		}
		
		//去掉最后一个逗号
		keys = keys.substring(0, keys.length()-1);
		return keys;
	}
	
	/**
	 * 重写put方法，默认转化小驼峰命名
	 * 
	 * @param key
	 * @param value
	 */
	public Object put(String key, Object value){
		return this.put(key, value, true);
	}
	
	
	/**
	 * 重写put方法，转化小驼峰命名
	 * @param key
	 * @param value
	 * @param toCamel：true:将key转化为小驼峰，false:不转化
	 * @return
	 */
	public Object put(String key, Object value, boolean toCamel){
		String newKey;
		if (toCamel){
			newKey = underlineToCamel(key);	
		}else {
			newKey = key;
		}
		
		if (value==null){
			value = "";
		}
		
		return super.put(newKey, value);
	}
	
	/**
	 * 下划线转小驼峰
	 * @param param 
	 * @return
	 */
	public static String underlineToCamel(String param) {
		char UNDERLINE = '_';
		
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        
        int len = param.length();

        //不包含下划线的，需要检查并转化为小写
        if (!param.contains("_")){
        	boolean isUpper = false;
        	boolean isLow = false;
        	for (int i = 0; i < len; i++) {
        		char c = param.charAt(i);
        		if (Character.isLowerCase(c)){
        			isLow = true; //检查到小写
        		}else
        		if (Character.isUpperCase(c)){
        			isUpper = true; //检查到大写
        		}else if (Character.isDigit(c)) {
					isLow = true; //检查到数字就是小写
				}
        	}
        	
        	//全部是大写的，需要转化为小写
        	if (isUpper&&!isLow){
        		return param.toLowerCase();        		
        	}else {
        		//有大写，有小写，直接返回原值
        		return param;				
			}
        }
        
        StringBuilder sb = new StringBuilder(len);
        Boolean flag = false; // "_" 后转大写标志,默认字符前面没有"_"
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (c == UNDERLINE) {
                flag = true;
                continue;   //标志设置为true,跳过
            } else {
                if (flag == true) {
                    //表示当前字符前面是"_" ,当前字符转大写
                    sb.append(Character.toUpperCase(param.charAt(i)));
                    flag = false;  //重置标识
                } else {
                    sb.append(Character.toLowerCase(param.charAt(i)));
                }
            }
        }
        return sb.toString();
    }
	
	/**
	 * 小驼峰转下划线
	 * @param param
	 * @param toUpper： true：统一都转大写；false-统一转小写
	 * @return
	 */
	public static String camelToUnderline(String param, boolean toUpper) {
		char UNDERLINE = '_';
		
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        
        int len = param.length();

        //已经包含下划线的，返回原值
        if (param.contains("_")){
        	return param;
        }
        
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append(UNDERLINE);
            }
            if (toUpper) {
                sb.append(Character.toUpperCase(c));  //统一都转大写
            } else {
                sb.append(Character.toLowerCase(c));  //统一都转小写
            }
        }
        return sb.toString();
    }
	
	/**
	 * 将bean转化为map
	 * @param bean
	 * @return
	 */
	public static <T> QsMap beanToMap(T bean){
		QsMap qsMap = new QsMap();
		if (bean!=null){
			BeanMap beanMap = BeanMap.create(bean);
			for (Object key : beanMap.keySet()) {
				Object o = beanMap.get(key);
				//ConcurrentHashMap 不能put null 的原因是因为：无法分辨是key没找到的null还是有key值为null值，
				//页这在多线程里面是模糊不清的，所以压根就不让put null。
				if (key!=null){
					if (o==null){
						logger.error(key+" value is null. ");
						o = "";
					}
					qsMap.put(key+"", o);					
				}
            }
		}
		return qsMap;
	}
	
	/*private static void beanMapPut(QsMap qsMap, BeanMap beanMap, List<String> keys, String key){
		if (!keys.contains(key)){
			return;
		}
		
		Object object = qsMap.get(key);
		if (object instanceof Double){
			beanMap.put(key, qsMap.getDouble(key));
		}else if (object instanceof Short) {
			beanMap.put(key, Short.valueOf(qsMap.getString(key)));
		}else if (object instanceof Integer){
			try {
				beanMap.put(key, Short.parseShort(qsMap.getString(key)));				
			} catch (Exception e) {
				e.printStackTrace();
				beanMap.put(key, qsMap.getString(key));
			}
		}else {
			beanMap.put(key, qsMap.getString(key));
		}
	}
	
	*//**
	 * 弃用
	 * @param map
	 * @param bean
	 * @return
	 *//*
	public static <T> T mapToBean(QsMap map, T bean){
		//TODO o instanceof Number ? ((Number) o).intValue() : (int) getDouble(key)
		BeanMap beanMap = BeanMap.create(bean);
		//beanMap.putAll(map);
		
		List<String> keys = new ArrayList<>();
		for (Object set: beanMap.entrySet()){
			String newKey = set.toString().substring(0, set.toString().indexOf("="));
			keys.add(newKey);
		};
		
		String key;
        for(Iterator<String> it = map.keySet().iterator(); it.hasNext(); beanMapPut(map, beanMap, keys, key))
            key = (String) it.next();
        
		return bean;
	}*/
	
	/**
	 * 将map转化为Object
	 * @param map
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	public static void mapToObj(QsMap map, Object obj){
		try{
            DateTimeConverter dtConverter = new DateTimeConverter();
            ConvertUtilsBean convertUtilsBean = new ConvertUtilsBean();
            convertUtilsBean.deregister(Date.class);
            convertUtilsBean.register(dtConverter, Date.class);
            BeanUtilsBean beanUtilsBean = new BeanUtilsBean(convertUtilsBean, new PropertyUtilsBean());
            beanUtilsBean.populate(obj, map);
        }catch(Exception e){
            e.printStackTrace();
        }
	}
	
	public int getPageIndex(){
		Object o = get("pageIndex");
		if (o==null||o.equals("")){
			return 1; //默认第一页
		}else {
			return this.getInt("pageIndex");			
		}
	}
	
	public int getPageSize(){
		Object o = get("pageSize");
		if (o==null||o.equals("")){
			return 10; //默认10行
		}else {
			return this.getInt("pageSize");			
		}
	}
	
	
	/**
	 * //测试map与bean的转换
	 * PrjCmItem prjCmItem = new PrjCmItem();
		String string  = "{\"wgCode\":\"网格编码\",\"jkTotalLength\":\"555\",\"dwjjAssetAttr\":\"2\",\"dzmc\":\"变电站名称\",\"applyOrgCode\":\"41311016140100001\",\"xlTotalLength\":\"123\",\"applyId\":\"100053\",\"itemName\":\"太原解放南路66kV输变电工程\",\"prjProp\":\"\",\"areaName\":\"台区\",\"applyDeptId\":\"\",\"lowPressureYh\":\"666\",\"dzCount\":\"222\",\"voltLevel\":\"AC00661\",\"lineCount\":\"444\",\"feaStatus\":-3,\"area\":\"太原\",\"specialityIds\":\"1,2,3,4,5,6,7,8,9,10,11,12,13\",\"planStartTime\":\"2020-06-18\",\"pdxlmc\":\"配电线路名称\",\"isHelp\":\"1\",\"version\":\"预规版本是个啥\",\"itemPhase\":0,\"isExtend\":\"1\",\"conScale\":\"\",\"storageGrade\":\"\",\"buildType\":\"1\",\"deptCode\":\"41311016140100001\",\"itemCont\":\"\",\"itemType\":\"2\",\"jjitemSpec\":\"\",\"isAi\":\"1\",\"conDept\":\"41311016140100002\",\"questionCode\":\"010505200020031\",\"dlTotalLength\":\"111\",\"magType\":\"2\",\"isLowPressure\":\"1\",\"isFarmNet\":\"1\",\"speciality\":\"1\",\"planTcTime\":\"2020-06-09\",\"itemClass1\":\"10001001\",\"itemClass2\":\"10001001001\",\"itemClass3\":\"1000100100101\",\"stationName\":\"解放南路\",\"initialInvest\":0,\"extType\":\"\",\"feaAppDocNo\":\"\",\"fragmentaryTypeIds\":\"null,11,1,2,3,1,2,3,7,4,8,5,9,6,10,7,6,8,7,8,9,10\",\"singleItemType\":\"\",\"dwjjProAttr\":\"5\",\"dzCapacity\":\"333\",\"auditAdvice\":\"\",\"planTime\":\"\",\"itemClass\":\"2\",\"applyRoleId\":\"\",\"itemSource\":\"1\"}";
		QsMap param = QsMap.fromObject(string);
		QsMap.mapToObj(param, prjCmItem);
		PrjCmStItem prjCmStItem = new PrjCmStItem();
		QsMap.mapToObj(param, prjCmStItem);
		
		System.out.println(prjCmItem.toString());
		System.out.println(prjCmStItem.toString());
		
		QsMap itemMap = QsMap.beanToMap(prjCmItem);
		QsMap stMap = QsMap.beanToMap(prjCmStItem);
		
		for (Entry<String, Object> entry: itemMap.entrySet()){
			String key = entry.getKey();
			if (itemMap.containsKey(key)){
				Object itemO = entry.getValue();
				Object paramO = param.get(key);
				
				if (!itemO.equals(paramO)){
					System.out.println(key + " value is not equals. " + "| itemO : " + entry.getValue() + "| paramO : "  + param.get(key));
				}
			}
		}
		
		for (Entry<String, Object> entry: stMap.entrySet()){
			String key = entry.getKey();
			if (itemMap.containsKey(key)){
				Object stO = entry.getValue();
				Object paramO = param.get(key);
				
				if (!stO.equals(paramO)){
					System.out.println(key + " value is not equals. " + "| stO : " + entry.getValue() + "| paramO : "  + param.get(key));
				}
			}
		}
		
		System.out.println(itemMap.toString());
		System.out.println(stMap.toString());
		
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
	}	
}
