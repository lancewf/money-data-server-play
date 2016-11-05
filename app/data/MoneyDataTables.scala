package data

import org.joda.time.{DateTimeZone, DateTime}

object MoneyDataTables extends {
  val profile = scala.slick.driver.MySQLDriver
} with MoneyDataTables

/**
  * Created by lancewf on 2/3/16.
  */
trait MoneyDataTables {
  val profile: scala.slick.driver.JdbcProfile
  import profile.simple._
  import scala.slick.jdbc.{GetResult => GR}

  case class BillTypeRow(key:Int, userId:Int, name:String, description:String)

  implicit def GetResultBillTypeRow(implicit e0: GR[Int], e1: GR[String]): GR[BillTypeRow] = GR{
    prs => import prs._
      BillTypeRow.tupled((<<[Int], <<[Int], <<[String], <<[String]))
  }

  class BillType(tag: Tag) extends Table[BillTypeRow](tag, None, "billtype") {
    def * = (key, userId, name, description) <> (BillTypeRow.tupled, BillTypeRow.unapply)

    def ? = (key.?, userId.?, name.?, description.?).shaped.<>(
      {r=>import r._; _1.map(_=> BillTypeRow.tupled((_1.get, _2.get, _3.get, _4.get)))},
      (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    val key: Column[Int] = column[Int]("key")
    val userId: Column[Int] = column[Int]("user_id")
    val name: Column[String] = column[String]("name")
    val description: Column[String] = column[String]("description")
  }

  lazy val BillType = new TableQuery(tag => new BillType(tag))


  case class AllottedRow(key:Int, userId:Int, startDate:java.sql.Timestamp, endDate:java.sql.Timestamp, billTypeKey:Int, amount:Double){
    lazy val startDateTime:DateTime ={
      new DateTime(startDate.getTime(), DateTimeZone.UTC)
    }

    lazy val endDateTime:DateTime ={
      new DateTime(endDate.getTime(), DateTimeZone.UTC)
    }
  }

  implicit def GetResultAllottedRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.sql.Timestamp], e3: GR[Double]): GR[AllottedRow] = GR{
    prs => import prs._
      AllottedRow.tupled((<<[Int], <<[Int], <<[java.sql.Timestamp], <<[java.sql.Timestamp], <<[Int], <<[Double]))
  }

  class Allotted(tag: Tag) extends Table[AllottedRow](tag, None, "allotted") {
    def * = (key, userId, endDate, startDate, billTypeKey, amount) <> (AllottedRow.tupled, AllottedRow.unapply)

    def ? = (key.?, userId.?, endDate.?, startDate.?, billTypeKey.?, amount.?).shaped.<>(
      {r=>import r._; _1.map(_=> AllottedRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))},
      (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    val key: Column[Int] = column[Int]("key")
    val userId: Column[Int] = column[Int]("user_id")
    val endDate: Column[java.sql.Timestamp] = column[java.sql.Timestamp]("startdate")
    val startDate: Column[java.sql.Timestamp] = column[java.sql.Timestamp]("enddate")
    val billTypeKey: Column[Int] = column[Int]("billtype_key")
    val amount: Column[Double] = column[Double]("amount")
  }

  lazy val Allotted = new TableQuery(tag => new Allotted(tag))



  case class PurchaseRow(key:Int, userId:Int, store:String, cost:Double, date:java.sql.Timestamp, notes:String, billTypeKey:Int){
    lazy val dateTime:DateTime ={
      new DateTime(date.getTime(), DateTimeZone.UTC)
    }
  }

  implicit def GetResultPurchaseRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.sql.Timestamp], e3: GR[Double]): GR[PurchaseRow] = GR{
    prs => import prs._
      PurchaseRow.tupled((<<[Int], <<[Int], <<[String], <<[Double], <<[java.sql.Timestamp], <<[String], <<[Int]))
  }

  class Purchase(tag: Tag) extends Table[PurchaseRow](tag, None, "purchase") {
    def * = (key, userId, store, cost, date, notes, billTypeKey) <> (PurchaseRow.tupled, PurchaseRow.unapply)

    def ? = (key.?, userId.?, store.?, cost.?, date.?, notes.?, billTypeKey.?).shaped.<>(
      {r=>import r._; _1.map(_=> PurchaseRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))},
      (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    val key: Column[Int] = column[Int]("key")
    val userId: Column[Int] = column[Int]("user_id")
    val store: Column[String] = column[String]("store")
    val cost: Column[Double] = column[Double]("cost")
    val date: Column[java.sql.Timestamp] = column[java.sql.Timestamp]("date")
    val notes: Column[String] = column[String]("notes")
    val billTypeKey: Column[Int] = column[Int]("billtype_key")
  }

  lazy val Purchase = new TableQuery(tag => new Purchase(tag))
}
