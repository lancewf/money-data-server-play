package data

import java.sql.Timestamp

import org.joda.time.{Duration, DateTimeZone, DateTime}

import scala.slick.jdbc.StaticQuery
import scala.slick.driver.MySQLDriver.simple._
import data.MoneyDataTables._
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.StaticQuery

case class CurrentViewItem(billTypeKey:Int, allotted:Double, spent:Double, amountLeft:Double,
                           average:Double, amountLeftOfAverage:Double)

/**
  * Created by lancewf on 2/3/16.
  */
class MoneyDataQuery(databaseDataSource: javax.sql.DataSource) {

  private val slickdriver = scala.slick.driver.MySQLDriver
  private val db = slickdriver.simple.Database.forDataSource(databaseDataSource)
  private implicit val session = db.createSession

  /**
    * Close the current database connection.
    */
  def close() {
    session.close
  }

  def getBillTypes(): List[BillTypeRow] = {
    val query = for {billType <- BillType} yield {
      billType
    }

    query.list
  }

  def getAllotteds(): List[AllottedRow] = {
    val query = for {allotted <- Allotted} yield {
      allotted
    }

    query.list
  }

  def getAllottedsFromBillType(billTypeKey:Int): List[AllottedRow] = {
    val query = for {allotted <- Allotted
      if(allotted.billTypeKey === billTypeKey)} yield {
      allotted
    }

    query.list
  }

  def getPurchases(startDate: DateTime, endDate: DateTime): List[PurchaseRow] = {
    val startDateTimestamp = new Timestamp(startDate.withMillisOfSecond(0).getMillis)
    val endDateTimestamp = new Timestamp(endDate.withMillisOfSecond(0).getMillis)
    val query = for {purchase <- Purchase
                     if (purchase.date > startDateTimestamp)
                     if (purchase.date < endDateTimestamp)} yield {
      purchase
    }

    query.list
  }

  def getMatchingPurchases(cost:Double, date:DateTime): List[PurchaseRow] = {
    val startDate = date.minusDays(7)
    val endDate = date.plusDays(7)
    val startDateTimestamp = new Timestamp(startDate.withMillisOfSecond(0).getMillis)
    val endDateTimestamp = new Timestamp(endDate.withMillisOfSecond(0).getMillis)
    val query =
      for {purchase <- Purchase
           if(purchase.cost === cost)
           if(purchase.date > startDateTimestamp)
           if(purchase.date < endDateTimestamp)} yield {
      purchase
    }

    query.sortBy(_.date.asc).list
  }

  def getBillTypePurchases(billTypeKey:Int, startDate:DateTime, endDate:DateTime): List[PurchaseRow] = {
    val startDateTimestamp = new Timestamp(startDate.withMillisOfSecond(0).getMillis)
    val endDateTimestamp = new Timestamp(endDate.withMillisOfSecond(0).getMillis)
    val query =
      for {purchase <- Purchase
           if(purchase.billTypeKey === billTypeKey)
           if(purchase.date > startDateTimestamp)
           if(purchase.date < endDateTimestamp)} yield {
        purchase
      }

    query.list
  }

  def getPurchasesFromQuery(query: Query[Purchase, PurchaseRow, Seq]): List[PurchaseRow] = {
    query.list
  }

  def getPurchases(billTypeKey:Int): List[PurchaseRow] = {
    val query = for {purchase <- Purchase
      if(purchase.billTypeKey === billTypeKey)} yield {
      purchase
    }

    query.list
  }

  def getStores(): List[String] = {
    val query = for {purchase <- Purchase} yield {
      purchase.store
    }

    query.list.distinct
  }

  def modifyPurchase(purchaseKey:Int, store:String, cost:Double, month:Int, dayOfMonth:Int,
                     year:Int, note:String, billTypeKey:Int){
    val dateTime = new DateTime(year, month, dayOfMonth, 12, 30, DateTimeZone.UTC)
    val timestamp = new Timestamp(dateTime.withMillisOfSecond(0).getMillis)
    val query =
      for {purchase <- Purchase
        if(purchase.key === purchaseKey)} yield {
        (purchase.store, purchase.cost, purchase.notes, purchase.date, purchase.billTypeKey)
    }

    query.update(store, cost, note, timestamp, billTypeKey)
  }

  def deletePurchase(purchaseKey:Int){
    val query =
      for {purchase <- Purchase
           if(purchase.key === purchaseKey)} yield {purchase}

    query.delete
  }

  def addPurchase(store:String, cost:Double, month:Int,
                  dayOfMonth:Int, year:Int, note:String, billTypeKey:Int){
    val dateTime = new DateTime(year, month, dayOfMonth, 12, 30, DateTimeZone.UTC)
    val timestamp = new Timestamp(dateTime.withMillisOfSecond(0).getMillis)

    Purchase += PurchaseRow(-1, 653611718, store, cost, timestamp, note, billTypeKey)
  }

  def getCurrentAllotted(billTypeKey:Int):Option[AllottedRow] ={

    val now = DateTime.now()

    getAllottedsFromBillType(billTypeKey).find(allotted => allotted.startDateTime.isBefore(now) && allotted.endDateTime.isAfter(now))
  }
}
