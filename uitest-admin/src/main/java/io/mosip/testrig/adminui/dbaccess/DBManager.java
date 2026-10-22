package io.mosip.testrig.adminui.dbaccess;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;



import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
//import org.hibernate.HibernateException;
//import org.hibernate.Session;
//import org.hibernate.SessionFactory;
//import org.hibernate.cfg.Configuration;
//import org.hibernate.cfg.Environment;
//import org.hibernate.jdbc.Work;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.jdbc.Work;

import io.mosip.testrig.adminui.kernel.util.ConfigManager;
import io.mosip.testrig.adminui.utility.BaseClass;

public class DBManager extends BaseClass {

	private static Logger logger = Logger.getLogger(DBManager.class);

	public static void clearMasterDbData() {
		Session session = null;
		try {

			logger.info("DB URL:: " + ConfigManager.getMASTERDbUrl());
			logger.info("DbUser:: " + ConfigManager.getMasterDbUser());
			logger.info("DbPass:: " + ConfigManager.getMasterDbPass());
			logger.info("DbSchema:: " + ConfigManager.getMasterDbSchema());
			session = getDataBaseConnection(ConfigManager.getMASTERDbUrl(), ConfigManager.getMasterDbUser(),
					ConfigManager.getMasterDbPass(), ConfigManager.getMasterDbSchema());
			if (session != null) {
				session.doWork((Work) new Work() {

					@Override
					public void execute(Connection connection) throws SQLException {
						Statement statement = connection.createStatement();
						// To Do --- Read the delete queries from a file and iterate
						try {
							;
							statement.addBatch("delete from applicant_valid_document where cr_by ='"+BaseClass.userid+"'");
						//	statement.addBatch("delete from appl_form_type  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from biometric_attribute  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from biometric_type  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from blocklisted_words  where cr_by ='"+BaseClass.userid+"'");
//							statement.addBatch("delete from daysofweek_list  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch(
									"delete from device_master  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch(
									"delete from device_master_h  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch(
									"delete from device_spec  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from device_type  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from doc_category  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from doc_type  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from dynamic_field  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from language  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from location  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from loc_hierarchy_list  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from loc_holiday  where cr_by ='"+BaseClass.userid+"'");
							statement
									.addBatch("delete from machine_master  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from machine_master_h  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from machine_spec  where cr_by ='"+BaseClass.userid+"'");
							statement
									.addBatch("delete from machine_type  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from module_detail  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from reason_category  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from reason_list  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from registration_center  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from registration_center_h  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from reg_center_type  where cr_by ='"+BaseClass.userid+"'");
							//statement.addBatch("delete from reg_device_type  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from reg_exceptional_holiday  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from reg_working_nonworking  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from template  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from template_file_format  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from template_type  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from title  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from user_detail  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from user_detail_h  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from valid_document  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from zone  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from zone_user  where cr_by ='"+BaseClass.userid+"'");
							statement
									.addBatch("delete from zone_user_h  where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from blocklisted_words where cr_by ='"+BaseClass.userid+"'");
							statement.addBatch("delete from bulkupload_transaction where cr_by ='"+BaseClass.userid+"'");
							int[] result = statement.executeBatch();
							logger.info("Success:: Executed MASTER DB quiries successfully.");
							for (int i : result) {
								logger.info("master db deleted records: " + i);
							}
						} finally {
							statement.close();
						}

					}

				});
			}
		} catch (Exception e) {
			logger.error("Error:: While executing MASTER DB Quiries." + e.getMessage());
		} finally {
			if (session != null) {
				session.close();
			}
		}

	}

	private static Session getDataBaseConnection(String dburl, String userName, String password, String schema) {
		SessionFactory factory = null;
		Session session = null;
		
		logger.info("dburl is" + dburl);
		logger.info("userName is" + userName);
		logger.info("password is" + password);
		

		try {
			Configuration config = new Configuration();
			config.setProperty(Environment.DRIVER, ConfigManager.getDbDriverClass());
			config.setProperty(Environment.URL, dburl);
			logger.info("dburl is" + dburl);
			config.setProperty(Environment.USER, userName);
			logger.info("userName is" + userName);
			config.setProperty(Environment.PASS, password);
			logger.info("password is" + password);
			config.setProperty(Environment.DEFAULT_SCHEMA, schema);
			config.setProperty(Environment.POOL_SIZE, ConfigManager.getDbConnectionPoolSize());
			config.setProperty(Environment.DIALECT, ConfigManager.getDbDialect());
			config.setProperty(Environment.SHOW_SQL, ConfigManager.getShowSql());
			config.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, ConfigManager.getDbSessionContext());

			factory = config.buildSessionFactory();
			session = factory.getCurrentSession();
			session.beginTransaction();
		} catch (HibernateException | NullPointerException e) {
			logger.error("Error while getting the db connection for ::" + dburl);
		}
		return session;
	}

}

