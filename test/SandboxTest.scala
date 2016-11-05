import data.{QueryBuilder, MoneyDataQuery}
import org.junit._
import org.junit.Assert._

/**
  * Created by lancewf on 2/3/16.
  */
@Test
class SandboxTest {

  @Test
  def testOK() = assertTrue(true)

  @Test
  def testPullBillTypes() {
    QueryBuilder.withMoneyDataQuery(moneyDataQuery =>{
      val billTypes = moneyDataQuery.getStores()

      println(billTypes.mkString(","))
    })
  }
}
