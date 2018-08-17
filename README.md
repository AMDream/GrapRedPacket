# GrapRedPacket
使用redis实现高并发业务之抢红包
<hr>
高并发场景下要保证业务一致性：<br>
1.悲观锁：select * from t_table where id = XX for update
<br>
2.乐观锁：+version<br>
select * from t_table where id = XX;  假设此处version为3
<br>
update t_table field1 = XXX,version = version + 1<br>
where id = XX and version = 3
<br>
失败补偿策略：时间戳or尝试次数设置
<br>
3.使用redis，直到所有红包抢完，然后将所有抢红包信息入库
