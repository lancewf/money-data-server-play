package controllers

import _root_.data.MoneyDataTables.PurchaseRow
import _root_.data.{CurrentViewItemBuilder, PurchaseSearcher, QueryBuilder, RequestsSocketActor}
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}
import play.api._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc._
import play.api.Play.current

class Application extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.index("Money Data"))
  }

  //20160319040000.000
  private val parseDate = DateTimeFormat.forPattern("yyyyMMddHHmmss.sss").withZone(DateTimeZone.UTC)
  case class BankTransaction(store:String, cost:Double, date:DateTime)

  def upload = Action { request =>
    val body: AnyContent = request.body
    val jsonBodyOption: Option[JsValue] = body.asJson

    jsonBodyOption match{
      case Some(jsValue) =>{
        val name = (jsValue \ "filename")
        val xml = (jsValue \ "xml").as[String]

        val xmlResult = scala.xml.XML.loadString(xml)
        val bankTransactions = (for{statementTransaction <- (xmlResult \\ "STMTTRN")
                                   storeName = (statementTransaction \ "NAME").head.text
                                   rawCost = (statementTransaction \ "TRNAMT").head.text
                                   rawDate = (statementTransaction \ "DTPOSTED").head.text} yield {

          val date = parseDate.parseDateTime(rawDate)

          Json.obj(
            "store" -> storeName,
            "cost" -> rawCost.toDouble * -1,
            "date" -> date)
        }).toList
        Ok(Json.toJson(bankTransactions)).as("application/json")
      }
      case None =>{
        Ok("Error").as("application/json")
      }
    }
  }

  def getBillTypes = Action {
    QueryBuilder.withMoneyDataQuery(moneyDataQuery =>{

      val billTypes = for{billType <- moneyDataQuery.getBillTypes()} yield {
        Json.obj(
          "key" -> billType.key,
          "name" -> billType.name,
          "description" -> billType.description)
      }

      Ok(Json.toJson(billTypes).toString).as("application/json")
    })
  }

  def getAllocatedAmounts = Action {
    QueryBuilder.withMoneyDataQuery(moneyDataQuery =>{

      val allocatedAmounts = for{allotted <- moneyDataQuery.getAllotteds()} yield {
        Json.obj(
          "key" -> allotted.key,
          "amount" -> allotted.amount,
          "billtypekey" -> allotted.billTypeKey,
          "startdayofmonth" -> allotted.startDateTime.getDayOfMonth(),
          "startmonth" -> allotted.startDateTime.getMonthOfYear(),
          "startyear" -> allotted.startDateTime.getYear(),
          "enddayofmonth" -> allotted.endDateTime.getDayOfMonth(),
          "endmonth" -> allotted.endDateTime.getMonthOfYear(),
          "endyear" -> allotted.endDateTime.getYear())
      }

      Ok(Json.toJson(allocatedAmounts).toString).as("application/json")
    })
  }

  def getPurchases(startYear:Int, startMonth:Int, startDayOfMonth:Int, endYear:Int, endMonth:Int, endDayOfMonth:Int) = Action {
    QueryBuilder.withMoneyDataQuery(moneyDataQuery =>{
      val startDate = new DateTime(startYear, startMonth, startDayOfMonth, 0, 0, DateTimeZone.UTC)
      val endDate = new DateTime(endYear, endMonth, endDayOfMonth, 23, 59, DateTimeZone.UTC)
      val purchases = for{purchase <- moneyDataQuery.getPurchases(startDate, endDate)} yield {
        Json.obj(
          "key" -> purchase.key,
          "store" -> purchase.store,
          "cost" -> purchase.cost,
          "notes" -> purchase.notes,
          "billtypekey" -> purchase.billTypeKey,
          "dayofmonth" -> purchase.dateTime.getDayOfMonth(),
          "month" -> purchase.dateTime.getMonthOfYear(),
          "year" -> purchase.dateTime.getYear())
      }

      Ok(Json.toJson(purchases).toString).as("application/json")
    })
  }

  def getStores = Action {
    QueryBuilder.withMoneyDataQuery(moneyDataQuery =>{

      val stores = moneyDataQuery.getStores()

      Ok(Json.toJson(stores).toString).as("application/json")
    })
  }

  def getBillTypePurchases(billTypeKey:Int) = Action {
    QueryBuilder.withMoneyDataQuery(moneyDataQuery =>{

      val purchases = for{purchase <- moneyDataQuery.getPurchases(billTypeKey)} yield {
        Json.obj(
          "key" -> purchase.key,
          "store" -> purchase.store,
          "cost" -> purchase.cost,
          "notes" -> purchase.notes,
          "billtypekey" -> purchase.billTypeKey,
          "dayofmonth" -> purchase.dateTime.getDayOfMonth(),
          "month" -> purchase.dateTime.getMonthOfYear(),
          "year" -> purchase.dateTime.getYear())
      }

      Ok(Json.toJson(purchases).toString).as("application/json")
    })
  }

  def modifyPurchase(purchaseKey:Int, store:String, cost:Double, month:Int,
                     dayOfMonth:Int, year:Int, note:String, billTypeKey:Int) = Action {
    QueryBuilder.withMoneyDataQuery(moneyDataQuery =>{
      try {
        moneyDataQuery.modifyPurchase(purchaseKey, store, cost, month, dayOfMonth, year, note, billTypeKey)

        Ok(Json.toJson("Success").toString).as("application/json")
      } catch{
        case ex:Exception =>{
          Ok(Json.toJson("Failure").toString).as("application/json")
        }
      }
    })
  }

  def deletePurchase(purchaseKey:Int) = Action {
    QueryBuilder.withMoneyDataQuery(moneyDataQuery =>{
      try {
        moneyDataQuery.deletePurchase(purchaseKey)

        Ok(Json.toJson("Success").toString).as("application/json")
      } catch{
        case ex:Exception =>{
          Ok(Json.toJson("Failure").toString).as("application/json")
        }
      }
    })
  }

  def addPurchase(store:String, cost:Double, month:Int,
                     dayOfMonth:Int, year:Int, note:String, billTypeKey:Int) = Action {
    QueryBuilder.withMoneyDataQuery(moneyDataQuery =>{
      try {
        moneyDataQuery.addPurchase(store, cost, month, dayOfMonth, year, note, billTypeKey)

        Ok(Json.toJson("Success").toString).as("application/json")
      } catch{
        case ex:Exception =>{
          Ok(Json.toJson("Failure").toString).as("application/json")
        }
      }
    })
  }

  def getCurrentViewItems() = Action {
    QueryBuilder.withMoneyDataQuery(moneyDataQuery =>{
      val currentViewItemBuilder = new CurrentViewItemBuilder()
      try {
        val currentViewItems =
          for{currentViewItem <- currentViewItemBuilder.getCurrentViewItems(moneyDataQuery)} yield {
          Json.obj(
            "billType" -> currentViewItem.billTypeKey,
            "allotted" -> currentViewItem.allotted,
            "spent" -> currentViewItem.spent,
            "amountLeft" -> currentViewItem.amountLeft,
            "average" -> currentViewItem.average,
            "amountLeftOfAverage" -> currentViewItem.amountLeftOfAverage)
        }

        Ok(Json.toJson(currentViewItems).toString).as("application/json")
      } catch{
        case ex:Exception =>{
          ex.printStackTrace()
          Ok(Json.toJson("Failure: " + ex.getMessage).toString).as("application/json")
        }
      }
    })
  }

  def getMatchingPurchases(cost:Double, dateMilisec:Long) = Action {
    QueryBuilder.withMoneyDataQuery(moneyDataQuery => {
      try {
        val date = new DateTime(dateMilisec, DateTimeZone.UTC)
        val purchaseJsons = for{purchase <- moneyDataQuery.getMatchingPurchases(cost, date)} yield {
          Json.obj(
            "key" -> purchase.key,
            "store" -> purchase.store,
            "cost" -> purchase.cost,
            "notes" -> purchase.notes,
            "billtypekey" -> purchase.billTypeKey,
            "date" -> purchase.dateTime)
        }
        Ok(Json.toJson(purchaseJsons).toString).as("application/json")
      } catch {
        case ex: Exception => {
          Ok(Json.toJson("Failure").toString).as("application/json")
        }
      }
    })
  }

  def searchPurchases(startmonth:Option[Int], startdaymonth:Option[Int], startyear:Option[Int], endmonth:Option[Int],
                      enddaymonth:Option[Int], endyear:Option[Int], billtypekey:Option[Int], storename:Option[String],
                      cost:Option[Double], costcomparison:Option[String], costrange:Option[Double]) = Action {
    val purchaseSearcher = new PurchaseSearcher()

    val startDateOption = for{startmonth1 <- startmonth
                              startdaymonth1 <- startdaymonth
                              startyear1 <- startyear} yield {
      new DateTime(startyear1, startmonth1, startdaymonth1, 23, 59, DateTimeZone.UTC)
    }

    val endDateOption = for{endmonth1 <- endmonth
                            enddaymonth1 <- enddaymonth
                            endyear1 <- endyear} yield {
      new DateTime(endyear1, endmonth1, enddaymonth1, 0, 0, DateTimeZone.UTC)
    }

    QueryBuilder.withMoneyDataQuery(moneyDataQuery => {
      try {
        val purchases: Seq[PurchaseRow] = purchaseSearcher.findPurchases(moneyDataQuery, startDateOption,
          endDateOption, billtypekey, storename, cost, costcomparison, costrange)

        println("got data: " + purchases.size)
        val purchaseJsons = for {purchase <- purchases} yield {
          Json.obj(
            "key" -> purchase.key,
            "store" -> purchase.store,
            "cost" -> purchase.cost,
            "notes" -> purchase.notes,
            "billtypekey" -> purchase.billTypeKey,
            "dayofmonth" -> purchase.dateTime.getDayOfMonth(),
            "month" -> purchase.dateTime.getMonthOfYear(),
            "year" -> purchase.dateTime.getYear())
        }

        Ok(Json.toJson(purchaseJsons).toString).as("application/json")
      } catch {
        case ex: Exception => {
          Ok(Json.toJson("Failure").toString).as("application/json")
        }
      }
    })
  }

  def ws = WebSocket.acceptWithActor[String, String] { request => out =>
    RequestsSocketActor.props(out)
  }
}
