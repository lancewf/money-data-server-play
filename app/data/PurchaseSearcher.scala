package data

import java.sql.Timestamp

import data.MoneyDataTables._
import org.joda.time.DateTime
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.lifted.Column

/**
  * Created by lancewf on 2/14/16.
  */
class PurchaseSearcher {

  def findPurchases(moneyDataQuery:MoneyDataQuery, startDateOption:Option[DateTime], endDateOption:Option[DateTime],
                    billTypeKeyOption:Option[Int], storeNameOption:Option[String], costOption:Option[Double],
                    costComparisonTagOption:Option[String], costRangeOption:Option[Double]):List[PurchaseRow] ={
    val startDateTimestampOption = startDateOption.map(date => new Timestamp(date.withMillisOfSecond(0).getMillis))
    val endDateTimestampOption = endDateOption.map(date => new Timestamp(date.withMillisOfSecond(0).getMillis))

    val query = for {purchase <- Purchase
                     if (startDateBooleanBuilder(purchase, startDateTimestampOption))
                     if (endDateBooleanBuilder(purchase, endDateTimestampOption))
                     if (billTypeKeyOptionBooleanBuilder(purchase, billTypeKeyOption))
                     if (storeNameOptionBooleanBuilder(purchase, storeNameOption))
                     if (costOptionBooleanBuilder(purchase, costOption, costComparisonTagOption, costRangeOption))
    } yield {
      purchase
    }

    moneyDataQuery.getPurchasesFromQuery(query)
  }

  private def costOptionBooleanBuilder(purchase:Purchase, costOption:Option[Double],
                costComparisonTagOption:Option[String], costRangeOption:Option[Double]):Column[Boolean] ={

    if(costComparisonTagOption.nonEmpty && costOption.nonEmpty){
      costComparison(purchase, costComparisonTagOption.get, costOption.get, costRangeOption)
    } else{
      LiteralColumn[Boolean](true)
    }
  }

  private def storeNameOptionBooleanBuilder(purchase:Purchase, storeNameOption:Option[String]):Column[Boolean] ={
    storeNameOption match{
      case Some(storeName) =>{
        purchase.store === storeName
      }
      case None =>{
        LiteralColumn[Boolean](true)
      }
    }
  }

  private def billTypeKeyOptionBooleanBuilder(purchase:Purchase, billTypeKeyOption:Option[Int]):Column[Boolean] ={
    billTypeKeyOption match{
      case Some(billTypeKey) =>{
        purchase.billTypeKey === billTypeKey
      }
      case None =>{
        LiteralColumn[Boolean](true)
      }
    }
  }

  private def startDateBooleanBuilder(purchase:Purchase, startDateTimestampOption:Option[Timestamp]):Column[Boolean] ={
    startDateTimestampOption match{
      case Some(startDateTimestamp) =>{
        purchase.date > startDateTimestamp
      }
      case None =>{
        LiteralColumn[Boolean](true)
      }
    }
  }

  private def endDateBooleanBuilder(purchase:Purchase, endDateTimestampOption:Option[Timestamp]):Column[Boolean] ={
    endDateTimestampOption match{
      case Some(endDateTimestamp) =>{
        purchase.date < endDateTimestamp
      }
      case None =>{
        LiteralColumn[Boolean](true)
      }
    }
  }

  private def costComparison(purchase:Purchase, costComparisonTag:String,
                             cost:Double, costRangeOption:Option[Double]): Column[Boolean] = {
    costComparisonTag match{
      case "Equal" =>{
        purchase.cost === cost
      }
      case "Greater" =>{
        purchase.cost > cost
      }
      case "Less" =>{
        purchase.cost < cost
      }
      case "Range" =>{
        costRangeOption match{
          case Some(costRange) =>{
            purchase.cost > cost && purchase.cost < costRange
          }
          case None =>{
            LiteralColumn[Boolean](true)
          }
        }
      }
    }
  }
}
