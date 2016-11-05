package data

import data.MoneyDataTables.{AllottedRow, BillTypeRow}
import org.joda.time.{DateTimeZone, DateTime}

/**
  * Created by lancewf on 2/7/16.
  */
class CurrentViewItemBuilder {

  def getCurrentViewItems(moneyDataQuery:MoneyDataQuery):List[CurrentViewItem] ={
    val billTypes = moneyDataQuery.getBillTypes()

    for{billType <- billTypes
        currentAllotted <- moneyDataQuery.getCurrentAllotted(billType.key)
        if(currentAllotted.amount > 0.0)} yield {

      val spent = calculateTotalPurcahseAmountCurrentMonth(billType, moneyDataQuery)

      val averageAmount = calculateAverageSpentAmount(billType, currentAllotted, moneyDataQuery)

      val amountLeft = currentAllotted.amount - spent

      val amountLeftOfAverage = getAmountLeftForAverage(billType, moneyDataQuery)

      CurrentViewItem(billType.key, currentAllotted.amount, spent, amountLeft, averageAmount, amountLeftOfAverage)
    }
  }

  private def calculateTotalPurcahseAmountCurrentMonth(billType:BillTypeRow,
                                               moneyDataQuery:MoneyDataQuery):Double ={

    val now = DateTime.now()
    val startOfMonth = new DateTime(now.getYear, now.getMonthOfYear, 1, 0, 0, DateTimeZone.UTC)
    val endOfMonth = startOfMonth.plusMonths(1)

    val purchases = moneyDataQuery.getBillTypePurchases(billType.key, startOfMonth, endOfMonth)

    purchases.map(_.cost).sum
  }

  private def calculateAverageSpentAmount(billType:BillTypeRow, allottedRow:AllottedRow, moneyDataQuery:MoneyDataQuery):Double ={

    val startDateTime = allottedRow.startDateTime
    val now = DateTime.now()

    val startNumberOfMonths = startDateTime.getMonthOfYear + startDateTime.getYear * 12
    val nowNumberOfMonths = now.getMonthOfYear + now.getYear * 12

    val totalNumberOfMonthsAllotted = if(now.getDayOfMonth > 14){
      nowNumberOfMonths - startNumberOfMonths + 1
    } else{
      nowNumberOfMonths - startNumberOfMonths
    }

    val total = moneyDataQuery.getBillTypePurchases(billType.key, allottedRow.startDateTime, now).map(_.cost).sum

    total / totalNumberOfMonthsAllotted
  }

  private def getAmountLeftForAverage(billType: BillTypeRow, moneyDataQuery: MoneyDataQuery): Double = {

    (for {allotted <- moneyDataQuery.getAllottedsFromBillType(billType.key)} yield {
      getAmountLeftForAverageFromAllotted(billType, allotted, moneyDataQuery)
    }).sum
  }

  private def getAmountLeftForAverageFromAllotted(billType:BillTypeRow, allotted:AllottedRow,
                                          moneyDataQuery: MoneyDataQuery):Double ={
    val now = DateTime.now()

    val endNumberOfMonths = if(allotted.endDateTime.isAfter(now)){
      if(now.getDayOfMonth > 14) {
        now.getMonthOfYear + now.getYear * 12 + 1
      } else{
        now.getMonthOfYear + now.getYear * 12
      }
    } else{

      allotted.endDateTime.getMonthOfYear + allotted.endDateTime.getYear * 12
    }

    val startNumberOfMonths = allotted.startDateTime.getMonthOfYear + allotted.startDateTime.getYear * 12

    val totalNumberOfMonthsAllotted = endNumberOfMonths - startNumberOfMonths

    val totalPurchases = moneyDataQuery.getBillTypePurchases(billType.key, allotted.startDateTime, allotted.endDateTime).map(_.cost).sum

    val allottedTotal = allotted.amount * totalNumberOfMonthsAllotted.toDouble

    allottedTotal - totalPurchases
  }
}
