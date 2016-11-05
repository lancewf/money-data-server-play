package data

import com.mchange.v2.c3p0.ComboPooledDataSource
import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by lancewf on 2/4/16.
  */
object QueryBuilder {
  private val dataSource = buildDataSource

  /**
    * Create a connection to the database.
    */
  def withMoneyDataQuery[A](op: MoneyDataQuery => A): A = {
    val moneyDataQuery = new MoneyDataQuery(dataSource)
    try {
      op(moneyDataQuery)
    } finally {
      moneyDataQuery.close() // close database connection.
    }
  }

  private def buildDataSource():ComboPooledDataSource ={
    val config = ConfigFactory.load.getConfig("database")
    val userName = getUserName(config)
    val password = getPassword(config)
    val jdbcUrl = getJdbcUrl(config)
    val ds = new ComboPooledDataSource
    ds.setDriverClass("com.mysql.jdbc.Driver")
    ds.setJdbcUrl(jdbcUrl)
    ds.setUser(userName)
    ds.setPassword(password)

    ds
  }

  private def getJdbcUrl(config:Config):String ={
    if(config.hasPath("jdbc_url")){
      config.getString("jdbc_url")
    } else{
      "jdbc:postgresql://oltp.axiom:5432/oikos"
    }
  }

  private def getUserName(config:Config):String = {
    if(config.hasPath("user_name")){
      config.getString("user_name")
    } else{
      "oikos"
    }
  }

  private def getPassword(config:Config):String = {
    if(config.hasPath("password")){
      config.getString("password")
    } else{
      "bertrand"
    }
  }
}
