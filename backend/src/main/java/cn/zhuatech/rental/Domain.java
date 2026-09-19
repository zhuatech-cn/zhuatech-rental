/* 上海如静知华信息科技有限公司 https://www.zhuatech.cn/ */
package cn.zhuatech.rental;
import org.springframework.stereotype.Component;
import java.util.*;
import java.math.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import static cn.zhuatech.rental.Model.*;
import static cn.zhuatech.rental.Engine.*;
/**
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
@Component public class Domain {
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static Map<String,Object> copy(Row r){return new LinkedHashMap<>(r.data());}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static BigDecimal n(Row r,String k){return num(r.data(),k);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static BigDecimal z(Map<String,Object>d,String k){return d.containsKey(k)?num(d,k):BigDecimal.ZERO;}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static String t(Row r,String k){return txt(r.data(),k);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static List<Row> linked(Engine e,User u,String module,String key,String id){return e.all(u,module).stream().filter(r->t(r,key).equals(id)).toList();}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static void unique(Engine e,User u,String module,Map<String,Object>d,String key){require(e.all(u,module).stream().noneMatch(r->t(r,key).equalsIgnoreCase(txt(d,key))),"重复的"+key);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static void dates(Map<String,Object>d,String from,String to){require(!date(d,to).isBefore(date(d,from)),"结束日期不能早于开始日期");}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static void change(Engine e,User u,Row row,String state,Map<String,Object>d,String note){e.save(u,row,state,d,"LINKED",note);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public void edit(Engine e,User u,Row r,Map<String,Object>d){
  if(r.module().equals("readings")){require(e.ref(u,r.data(),"job","jobs").state().equals("RUNNING")&&txt(d,"job").equals(t(r,"job")),"仅进行中的任务可以修改测量值，且不得迁移任务");require(linked(e,u,"readings","job",t(r,"job")).stream().noneMatch(x->!x.id().equals(r.id())&&t(x,"point").equals(txt(d,"point"))),"测量点编号重复");return;}
  if(r.module().equals("versions")){require(e.ref(u,r.data(),"artwork","artworks").state().equals("DRAFT"),"已送审稿件不可修改");require(txt(d,"artwork").equals(t(r,"artwork"))&&num(d,"revision").compareTo(n(r,"revision"))==0,"版本不能迁移任务或改写版本号");return;}
  for(var m:e.spec().modules())for(Row other:e.all(u,m.key()))if(!other.id().equals(r.id())&&other.data().values().stream().anyMatch(v->r.id().equals(v)))throw new Failure(409,"资料已有下游引用，请新建版本而不是改写历史");
  var fields=e.spec().module(r.module()).fields().stream().map(Field::key).toList();
  r.data().forEach((k,v)->{if(!fields.contains(k))d.put(k,v);});
  if(d.containsKey("start")&&d.containsKey("end"))dates(d,"start","end");
  if(d.containsKey("from")&&d.containsKey("to"))dates(d,"from","to");
  for(String key:List.of("serial","sku","invoice","invoiceNo","lockNo"))if(d.containsKey(key))require(e.all(u,r.module()).stream().noneMatch(x->!x.id().equals(r.id())&&t(x,key).equalsIgnoreCase(txt(d,key))),"重复唯一业务标识: "+key);
  if(d.containsKey("bonusRate"))require(num(d,"bonusRate").compareTo(num(d,"baseRate"))>=0,"达档返利率不能低于基础返利率");
  if(d.containsKey("lifeLimit"))require(num(d,"serviceEvery").compareTo(num(d,"lifeLimit"))<=0,"保养间隔不能大于寿命");
  if(d.containsKey("defects"))require(num(d,"defects").compareTo(num(d,"shots"))<=0,"不良数不能超过生产次数");
  if(d.containsKey("nps"))require(num(d,"nps").compareTo(BigDecimal.TEN)<=0&&num(d,"csat").compareTo(new BigDecimal("5"))<=0,"评价分数超出范围");
  if(d.containsKey("oxygenMin"))require(num(d,"oxygenMin").compareTo(num(d,"oxygenMax"))<0,"氧气下限须小于上限");
  if(r.module().equals("invoices"))require(e.all(u,"invoices").stream().noneMatch(x->!x.id().equals(r.id())&&t(x,"shipment").equals(txt(d,"shipment"))),"运单已关联结算账单");
  if(r.module().equals("sales")){Row program=e.ref(u,d,"program","programs");require(program.state().equals("ACTIVE")&&!date(d,"soldAt").isBefore(date(program.data(),"start"))&&!date(d,"soldAt").isAfter(date(program.data(),"end")),"协议状态或销售日期无效");}
  if(r.module().equals("jobs")){Row instrument=e.ref(u,d,"instrument","instruments"),standard=e.ref(u,d,"standard","standards");require(!instrument.state().equals("RETIRED")&&t(instrument,"unit").equals(t(standard,"unit")),"器具状态或计量单位无效");require(!date(d,"performedAt").isAfter(LocalDate.now()),"不能记录未来校准");}
  if(r.module().equals("permits")){require(ChronoUnit.DAYS.between(date(d,"start"),date(d,"end"))<=7,"许可最长七天");require(t(e.ref(u,d,"isolation","isolations"),"location").equals(txt(d,"location")),"隔离区域不匹配");}
  if(r.module().equals("responses")){require(e.all(u,"responses").stream().noneMatch(x->!x.id().equals(r.id())&&t(x,"survey").equals(txt(d,"survey"))&&t(x,"customer").equals(txt(d,"customer"))),"客户已存在该问卷反馈");require(t(e.ref(u,d,"customer","customers"),"consent").equals("YES"),"客户未允许反馈邀请");}
  if(r.module().equals("products")){String barcode=txt(d,"barcode");require(barcode.matches("\\d{13}"),"条码须为 EAN-13");int sum=0;for(int x=0;x<12;x++)sum+=(barcode.charAt(x)-'0')*(x%2==0?1:3);require((10-sum%10)%10==barcode.charAt(12)-'0',"EAN-13 校验位不正确");}

 }
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public Map<String,Object> metrics(Engine e,User u){
  var out=new LinkedHashMap<String,Object>();out.put("可租设备",e.all(u,"equipment").stream().filter(r->r.state().equals("AVAILABLE")).count());out.put("履约合同",e.all(u,"contracts").stream().filter(r->r.state().equals("ACTIVE")).count());out.put("净收款",e.all(u,"payments").stream().map(r->t(r,"kind").equals("RECEIPT")?n(r,"amount"):n(r,"amount").negate()).reduce(BigDecimal.ZERO,BigDecimal::add));;return out;
 }
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public void create(Engine e,User u,String module,Map<String,Object>d){switch(module){case "equipment" -> unique(e,u,module,d,"serial");
case "contracts" -> {dates(d,"start","end");require(ChronoUnit.DAYS.between(date(d,"start"),date(d,"end"))<3660,"租期不能超过十年");d.put("received",0);d.put("refunded",0);} default -> {} }}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public String action(Engine e,User u,Row r,String action,Map<String,Object>i,Map<String,Object>d){
  String k=r.module()+"."+action;switch(k){
case "equipment.maintenance" -> require(e.all(u,"contracts").stream().noneMatch(c->t(c,"equipment").equals(r.id())&&Set.of("RESERVED","ACTIVE").contains(c.state())),"设备仍有占期合同");
case "contracts.reserve" -> {
 Row asset=e.ref(u,d,"equipment","equipment");require(asset.state().equals("AVAILABLE"),"设备不可租用");
 LocalDate start=date(d,"start"),end=date(d,"end");
 require(e.all(u,"contracts").stream().noneMatch(c->!c.id().equals(r.id())&&t(c,"equipment").equals(asset.id())&&Set.of("RESERVED","ACTIVE").contains(c.state())&&!date(c.data(),"end").isBefore(start)&&!date(c.data(),"start").isAfter(end)),"设备租期冲突");
 long days=ChronoUnit.DAYS.between(start,end)+1;d.put("days",days);d.put("dailyRate",n(asset,"dailyRate"));d.put("rent",money(n(asset,"dailyRate").multiply(BigDecimal.valueOf(days))));d.put("deposit",n(asset,"deposit"));
}
case "contracts.receive" -> {
 BigDecimal amount=num(i,"amount");require(z(d,"received").add(amount).compareTo(z(d,"rent").add(z(d,"deposit")).add(z(d,"lateFee")).add(z(d,"damageFee")))<=0,"收款超过应收金额");
 require(e.all(u,"payments").stream().noneMatch(p->t(p,"reference").equals(txt(i,"reference"))),"收款凭证重复");
 d.put("received",z(d,"received").add(amount));e.ledger(u,"payments","POSTED",Map.of("contract",r.id(),"amount",amount,"kind","RECEIPT","reference",txt(i,"reference")));return r.state();
}
case "contracts.dispatch" -> {
 Row asset=e.ref(u,d,"equipment","equipment");require(asset.state().equals("AVAILABLE"),"设备当前不可出库");
 Row customer=e.ref(u,d,"customer","customers");BigDecimal debt=z(d,"rent").add(z(d,"deposit")).subtract(z(d,"received"));
 for(Row c:linked(e,u,"contracts","customer",customer.id()))if(!c.id().equals(r.id())&&Set.of("ACTIVE","RETURNED").contains(c.state()))debt=debt.add(z(c.data(),"rent").add(z(c.data(),"lateFee")).add(z(c.data(),"damageFee")).subtract(z(c.data(),"received")).max(BigDecimal.ZERO));
 require(z(d,"received").compareTo(z(d,"deposit"))>=0,"出库前须收足押金");require(debt.compareTo(n(customer,"creditLimit"))<=0,"客户赊欠余额超限");
 change(e,u,asset,"ON_RENT",copy(asset),"合同交付 "+r.code());e.ledger(u,"movements","POSTED",Map.of("contract",r.id(),"equipment",asset.id(),"kind","DISPATCH"));
}
case "contracts.return" -> {
 LocalDate returned=date(i,"returnedAt");require(!returned.isBefore(date(d,"start")),"归还不能早于租期开始");long extra=Math.max(0,ChronoUnit.DAYS.between(date(d,"end"),returned));
 d.putAll(i);d.put("lateFee",money(z(d,"dailyRate").multiply(BigDecimal.valueOf(extra))));d.put("finalCharge",money(z(d,"rent").add(z(d,"lateFee")).add(z(d,"damageFee"))));
 Row asset=e.ref(u,d,"equipment","equipment");change(e,u,asset,"AVAILABLE",copy(asset),"退租释放设备");e.ledger(u,"movements","POSTED",Map.of("contract",r.id(),"equipment",asset.id(),"kind","RETURN"));
}
case "contracts.refund" -> {
 BigDecimal amount=num(i,"amount"),surplus=z(d,"received").subtract(z(d,"finalCharge")).subtract(z(d,"refunded"));
 require(amount.compareTo(surplus)<=0,"退款超过可退余额");require(e.all(u,"payments").stream().noneMatch(p->t(p,"reference").equals(txt(i,"reference"))),"资金凭证重复");
 d.put("refunded",z(d,"refunded").add(amount));e.ledger(u,"payments","POSTED",Map.of("contract",r.id(),"amount",amount,"kind","REFUND","reference",txt(i,"reference")));
}
case "contracts.settle" -> require(z(d,"received").subtract(z(d,"refunded")).compareTo(z(d,"finalCharge"))==0,"未完成应收补款或押金退款");
case "contracts.cancel" -> require(z(d,"received").signum()==0,"已有收款，需履约退租结算，不能直接撤销");
 default -> {} }return null;
 }
}
