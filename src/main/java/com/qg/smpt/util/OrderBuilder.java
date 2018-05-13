package com.qg.smpt.util;


import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import com.qg.smpt.printer.model.BConstants;
import com.qg.smpt.share.ShareMem;
import com.qg.smpt.web.model.Item;
import com.qg.smpt.web.model.Order;
import com.qg.smpt.web.model.User;
import com.qg.smpt.web.repository.UserMapper;
import com.qg.smpt.web.service.UserService;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class OrderBuilder {
	private static final Logger LOGGER = Logger.getLogger(OrderBuilder.class);

	private static List<User> users = null;

	private static int userCount = 0;
	
	public OrderBuilder() {

	}

	
	static {
		LOGGER.log(Level.DEBUG, "�������������ڳ�ʼ���̼���Ϣ", OrderBuilder.class);

		SqlSessionFactory sqlSessionFactory = SqlSessionFactoryBuild.getSqlSessionFactory();
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try {
			UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

			users = userMapper.selectAllUser();
		} finally {
			sqlSession.close();
		}

		if(users != null)
			userCount = users.size();
		else
			users = new ArrayList<>();
	}
	
	
	private static int num = 0;
	
	//��˾����
	private static String[] companys = {"��������", "������", "�ٶ�����", "�ٶ�����"};
	
//	//�̼���Ϣ
//	private static String shops[] = {"����", "�ϵ»�", "���ѳԵĵط�", "�벻��ʲô�ط���"};
//	private static String address[] = {"gogo�����һ¥25��","�����ҵ��", "�㶫��ҵ��ѧ����", "��ַδ֪" };
//	private static String contact[] = {"85241523", "84523651", "15521232546", "15622365842"};
	
	//�˵�
	private static String dish[] = {"������������", "���Ѹɳ�ľ��", "���ϸɳ��㽶", "������������", "��ó�ͯ�Ӽ�"};
	private static int prices[] = {4, 4, 5, 3, 8};
	
	//�˿���Ϣ
	private static String customers[] = {"�¿���", "������", "��ʫ��", "��ΰ��", "����ï", "����"};
	private static String cAddress[] = {"�㹤����736", "�㹤����612", "�㹤��ʮ��xxx", "�㹤����741", "�㹤����747", "�㹤����2xx"};
	private static String cContact[] = {"15521256251", "18852423652", "13432252452", "15622365455", "18819255400", "15695542562"};
	
	private static String remarks[] = {"�ӷ�", "�����", "�Ӳ�"};
	
	private static String expectTimes[] = {"10:30", "11.30", "12.30", "5.30", "6.30", "7.30"};

	/**
	 * ���������� flag 0-�ǼӼ��� 1-�Ӽ�
	 * @param flag
	 * @return
     */
	public static Order produceOrder(boolean flag, boolean hasError)  {
		Order order = new Order();
		order.setHasError(hasError);

		int randomNum = 0;
		
		//���˾��Ϣ
		randomNum = getRandom(4);
		order.setCompany(companys[randomNum]);
		
		//�����̼���Ϣ
//		randomNum = getRandom(4);
		if(userCount > 0){
			randomNum = getRandom(userCount);
			User u = users.get(randomNum);
			order.setClientName(u.getUserStore());
			order.setClientAddress(u.getUserAddress());
			order.setClientTelephone(u.getUserPhone());
			order.setOrderStatus(String.valueOf(BConstants.orderWait));
		}
		
		//��ȡ������Ϣ
		order.setId(++ShareMem.currentOrderNum);

		order.setOrderTime((new Date()));
		order.setExpectTime(expectTimes[getRandom(6)]);
		order.setOrderRemark(remarks[getRandom(3)]);
		
		//���ɹ˿���Ϣ
		randomNum = getRandom(6);
		order.setUserName(customers[randomNum]);
		order.setUserAddress(cAddress[randomNum]);
		order.setUserTelephone(cContact[randomNum]);
		order.setOrderStatus(Integer.valueOf(BConstants.orderWait).toString());

		//���ɲ�
//		randomNum = getRandom(5) + 1;
		randomNum = 1;
		List<Item> items = new ArrayList<Item>(randomNum);
		for(int i = 0; i < randomNum; i++){

//			int a = getRandom(10) + 1;
//			if (a%2==0) a = 20;
//			else a = 1;
//			for (int b = 0; b < a; b++)
					items.add(createItem(i));

		}
		order.setItems(items);
		
		//��������������Ϣ
		order.setOrderMealFee(getMealCost());
		order.setOrderDisFee(getdeliveryCost());
		order.setOrderPreAmount(getRandom(6));
		order.setOrderPayStatus("��֧��");

		// �ж��Ƿ����üӼ�
		if (flag) {
			order.setOrderType('1');
		} else {
			order.setOrderType('0');
		}
		return order;
	}

	/**
	 * ���ֿ���д���ṩ��Ƕ��ʽָ����С����
	 * @param size
	 * @param flag
	 * @param hasError
	 * @return
	 */
	public static Order produceOrder(double size,boolean flag, boolean hasError) {
		Order order = new Order();
		order.setHasError(hasError);

		int randomNum = 0;

		//���˾��Ϣ
		randomNum = getRandom(4);
		order.setCompany(companys[randomNum]);

		//�����̼���Ϣ
//		randomNum = getRandom(4);
		if (userCount > 0) {
			randomNum = getRandom(userCount);
			User u = users.get(randomNum);
			order.setClientName(u.getUserStore());
			order.setClientAddress(u.getUserAddress());
			order.setClientTelephone(u.getUserPhone());
			order.setOrderStatus(String.valueOf(BConstants.orderWait));
		}

		//��ȡ������Ϣ
		order.setId(++ShareMem.currentOrderNum);

		order.setOrderTime((new Date()));
		order.setExpectTime(expectTimes[getRandom(6)]);
		order.setOrderRemark(remarks[getRandom(3)]);

		//���ɹ˿���Ϣ
		randomNum = getRandom(6);
		order.setUserName(customers[randomNum]);
		order.setUserAddress(cAddress[randomNum]);
		order.setUserTelephone(cContact[randomNum]);
		order.setOrderStatus(Integer.valueOf(BConstants.orderWait).toString());

		//���ɲ�
		List<Item> items = null;
		//��byte��ת�ɲ˵���Ŀ
		if (size != 0) {
			if(size <= 1 && size >= 0.57){
				size -= 0.57;
				size *= 38;
			}else {
				size = 36 *(size - 1);
				size += 16;
			}
			size = size - size%1;
			LOGGER.log(Level.DEBUG," 1/size is " + size,OrderBuilder.class);
			items = new ArrayList<>((int) size);
		} else {
			//�����С�Ļ������Ҫ��һ��
			randomNum = getRandom(5);
			items = new ArrayList<>(150);
		}

		if (size == 0) {
			LOGGER.log(Level.DEBUG," 2/size is " + size,OrderBuilder.class);
			for (int i = 0; i < randomNum; i++) {
				int a = getRandom(10) + 1;
				if (a%2==0) a = 20;
				else a = 1;
				for (int b = 0; b < a; b++)
					items.add(createItem(i));
			}
		} else {
			for (int c = 0; c < size; c++) {
				items.add(createItem(1));
			}
		}
		order.setItems(items);
		LOGGER.log(Level.DEBUG,"items is " + items + "and size is " + size,OrderBuilder.class);
		//��������������Ϣ
		order.setOrderMealFee(getMealCost());
		order.setOrderDisFee(getdeliveryCost());
		order.setOrderPreAmount(getRandom(6));
		order.setOrderPayStatus("��֧��");

		// �ж��Ƿ����üӼ�
		if (flag) {
			order.setOrderType('1');
		} else {
			order.setOrderType('0');
		}
		return order;
	}

	public static Order produceOrder(boolean flag, boolean hasError, int index)  {
		Order order = new Order();
		order.setHasError(hasError);
		order.setIndexError(index);

		int randomNum = 0;

		//���˾��Ϣ
		randomNum = getRandom(4);
		order.setCompany(companys[randomNum]);

		//�����̼���Ϣ
//		randomNum = getRandom(4);
		if(userCount > 0){
			randomNum = getRandom(userCount);
			User u = users.get(randomNum);
			order.setClientName(u.getUserStore());
			order.setClientAddress(u.getUserAddress());
			order.setClientTelephone(u.getUserPhone());
			order.setOrderStatus(String.valueOf(BConstants.orderWait));
		}

		//��ȡ������Ϣ
		order.setId(++ShareMem.currentOrderNum);

		order.setOrderTime((new Date()));
		order.setExpectTime(expectTimes[getRandom(6)]);
		order.setOrderRemark(remarks[getRandom(3)]);

		//���ɹ˿���Ϣ
		randomNum = getRandom(6);
		order.setUserName(customers[randomNum]);
		order.setUserAddress(cAddress[randomNum]);
		order.setUserTelephone(cContact[randomNum]);
		order.setOrderStatus(Integer.valueOf(BConstants.orderWait).toString());

		//���ɲ�
		randomNum = getRandom(5) + 1;
		List<Item> items = new ArrayList<Item>(randomNum);
		for(int i = 0; i < randomNum; i++){
			items.add(createItem(i));
		}
		order.setItems(items);

		//��������������Ϣ
		order.setOrderMealFee(getMealCost());
		order.setOrderDisFee(getdeliveryCost());
		order.setOrderPreAmount(getRandom(6));
		order.setOrderPayStatus("��֧��");

		// �ж��Ƿ����üӼ�
		if (flag) {
			order.setOrderType('1');
		} else {
			order.setOrderType('0');
		}
		return order;
	}

	public static Order produceOrder(boolean flag, boolean hasError, int index, int textNum)  {
		Order order = new Order();
		order.setTextNum(textNum);
		order.setHasError(hasError);
		order.setIndexError(index);

		int randomNum = 0;

		//���˾��Ϣ
		randomNum = getRandom(4);
		order.setCompany(companys[randomNum]);

		//�����̼���Ϣ
//		randomNum = getRandom(4);
		if(userCount > 0){
			randomNum = getRandom(userCount);
			User u = users.get(randomNum);
			order.setClientName(u.getUserStore());
			order.setClientAddress(u.getUserAddress());
			order.setClientTelephone(u.getUserPhone());
			order.setOrderStatus(String.valueOf(BConstants.orderWait));
		}

		//��ȡ������Ϣ
		order.setId(++ShareMem.currentOrderNum);

		order.setOrderTime((new Date()));
		order.setExpectTime(expectTimes[getRandom(6)]);
		order.setOrderRemark(remarks[getRandom(3)]);

		//���ɹ˿���Ϣ
		randomNum = getRandom(6);
		order.setUserName(customers[randomNum]);
		order.setUserAddress(cAddress[randomNum]);
		order.setUserTelephone(cContact[randomNum]);
		order.setOrderStatus(Integer.valueOf(BConstants.orderWait).toString());

		//���ɲ�
		randomNum = getRandom(5) + 1;
		List<Item> items = new ArrayList<Item>(randomNum);
		for(int i = 0; i < randomNum; i++){
			items.add(createItem(i));
		}
		order.setItems(items);

		//��������������Ϣ
		order.setOrderMealFee(getMealCost());
		order.setOrderDisFee(getdeliveryCost());
		order.setOrderPreAmount(getRandom(6));
		order.setOrderPayStatus("��֧��");

		// �ж��Ƿ����üӼ�
		if (flag) {
			order.setOrderType('1');
		} else {
			order.setOrderType('0');
		}
		return order;
	}

	private static int getMealCost() {
		return getRandom(3);
	}
	
	private static int getdeliveryCost() {
		return getRandom(6);
	}
	
	private static int getOrderNum() {
		return  ++num;
	}
	
	private static Item createItem(int i){
		Item item = new Item();
		item.setName(dish[i]);
		item.setPrice(prices[i]);
		item.setCount(1);//getRandom(5) + 1
		return item;
	}
	
	private static String createRemark() {
		return remarks[getRandom(3)];
	}
	
	private static int getRandom(int seed) {
		return (int)(Math.random() * seed);
	}
}