package com.example.payumoneytest

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.checkout.base.model.Environment
import com.checkout.frames.api.PaymentFormMediator
import com.checkout.frames.screen.paymentform.PaymentFormConfig
import com.checkout.frames.style.screen.PaymentFormStyle
import com.example.payumoneytest.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    lateinit var payment_id: String
    lateinit var token: String
    lateinit var encrypted_cvv: String
    lateinit var rowData: String

    lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Form Configration
        val paymentFormConfig = PaymentFormConfig(
            publicKey = "pk_sbox_wqba4yv2ncen2a4g5obkbqj2bq",                     // set your public key
            context = this,                          // set context
            environment = Environment.SANDBOX,          // set the environment
            paymentFlowHandler = paymentFlowHandler,    // set the callback
            style = PaymentFormStyle(),                 // set the style
            supportedCardSchemeList = emptyList()       // set supported card schemes, by default uses all schemes
        )

         val paymentFormMediator = PaymentFormMediator(paymentFormConfig)

        binding.composeView.apply {
            setContent {
                paymentFormMediator.PaymentForm()
            }
        }

    }



    private fun getBankList(rowData: String) {
        val data = JSONArray(rowData)
        val methods = data.getJSONObject(0).getJSONArray("supported_payment_methods")
        val listOfBankInfo = mutableListOf<BankInfo>()
        for (i in 0 until methods.length()) {
            val method = methods.getJSONObject(i)
            val vendor = method.getString("vendor")
            if (vendor.equals("NetBanking")) {
                val displayName = method.getString("display_name")
                val sourceType = method.getString("source_type")
                val bankCode = method.getString("bank_code")
                val status = method.getString("status")
                val bank = BankInfo(displayName, vendor, bankCode, sourceType, status)
                listOfBankInfo.add(bank)
            }
        }

        listOfBankInfo.forEach { bank ->
            Log.d("GetBankList bank ", bank.toString() + "\n\n")
        }
    }

    private fun requestForBankList() {
        val requestQuery = Volley.newRequestQueue(this)

        val jsonObjectRequest =
            object :
                StringRequest(Method.GET, "https://api.paymentsos.com/supported-payment-methods", {
                    Toast.makeText(this, " success ", Toast.LENGTH_SHORT).show()
                    rowData = it.toString()
                    Log.e("bankList success", "> $rowData")
                    getBankList(rowData)
                }, {
                    Toast.makeText(this, " error > ${it.message}", Toast.LENGTH_SHORT).show()
                    Log.e("bankList error", "> ${it.message}")
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header["app_id"] = "com.ubuy.ubuy_india_test"
                    header["public_key"] = "0da634ec-a8ef-42bc-867b-5e970f18d55a"
                    header["Content-Type"] = "application/json"
                    header["api-version"] = "1.3.0"
                    header["x-payments-os-env"] = "test"
                    header["private_key"] = "39871bbc-1cea-42ee-8992-a981f471a1d0"
                    return header
                }
            }
        jsonObjectRequest.setRetryPolicy(
            DefaultRetryPolicy(
                30000, 3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
        )
        requestQuery.add(jsonObjectRequest)
    }

    data class BankInfo(
        val displayName: String,
        val vendor: String,
        val bankCode: String,
        val sourceType: String,
        val status: String
    )


    private fun createToken() {

        val requestQueue = Volley.newRequestQueue(this)

        val jsonObject = JSONObject()
        jsonObject.put("token_type", "credit_card")
        jsonObject.put("credit_card_cvv", "274")
        jsonObject.put("card_number", "4047457539765494")
        jsonObject.put("expiration_date", "02/26")
        jsonObject.put("holder_name", "OM PRAKASH")

        val jsonObjectRequest = object :
            JsonObjectRequest(Method.POST, "https://api.paymentsos.com/tokens", jsonObject, {
                Toast.makeText(this, " success ", Toast.LENGTH_SHORT).show()
                token = it.getString("token")
                encrypted_cvv = it.getString("encrypted_cvv")
                createPayment()
            }, {
                Toast.makeText(this, " error", Toast.LENGTH_SHORT).show()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val header = HashMap<String, String>()
                header["app_id"] = "com.ubuy.ubuy_india_live"
                header["public_key"] = "12aa286a-0f26-4f7c-a68b-4d5805b15e96"
                header["Content-Type"] = "application/json"
                header["api-version"] = "1.3.0"
                header["x-payments-os-env"] = "live"
                return header
            }
        }

        requestQueue.add(jsonObjectRequest)
    }

    fun createPayment() {
        val requestQueue = Volley.newRequestQueue(this)

        val billing_address = JSONObject()
        billing_address.put("phone", "8890024434")
        billing_address.put("country", "IND")
        billing_address.put("state", "Rajasthan")
        billing_address.put("city", "Jaipur")
        billing_address.put("email", "piyush.naranje@ubuy.com")
        billing_address.put("first_name", "kirti")
        billing_address.put("last_name", "jain")
        billing_address.put("zip_code", "302019")


        val payment = JSONObject()
        payment.put("amount", 200)
        payment.put("currency", "INR")
        payment.put("billing_address", billing_address)

        val jsonObjectRequest = object :
            JsonObjectRequest(Method.POST, "https://api.paymentsos.com/payments", payment, {
                payment_id = it.getString("id")
                Toast.makeText(this, " success", Toast.LENGTH_SHORT).show()
                getCharges()
            }, {
                Toast.makeText(this, "error ", Toast.LENGTH_SHORT).show()
            }
            ) {
            override fun getHeaders(): MutableMap<String, String> {
                val header = HashMap<String, String>()
                header["app-id"] = "com.ubuy.ubuy_india_live"
                header["private-key"] = "fb8cc210-00ea-4d1c-a8dd-38b761e09e28"
                header["Content-Type"] = "application/json"
                header["api-version"] = "1.3.0"
                header["x-payments-os-env"] = "live"
                return header
            }


        }

        requestQueue.add(jsonObjectRequest)
    }

    fun getCharges() {
        val paymentMethod = JSONObject()
        paymentMethod.put("type", "tokenized")
        paymentMethod.put("token", token)
        paymentMethod.put("encrypted_cvv", encrypted_cvv)

        val jsonObject = JSONObject()
        jsonObject.put("payment_method", paymentMethod)
        jsonObject.put("merchant_site_url", "https://www.ubuy.co.in/")

        val requestQueue = Volley.newRequestQueue(this)

        val jsonObjectRequest = object : JsonObjectRequest(Method.POST,
            "https://api.paymentsos.com/payments/$payment_id/charges",
            jsonObject,
            {
                Toast.makeText(this, " success ", Toast.LENGTH_SHORT).show()
                Log.d("getCharges: ", it.toString())
            },
            {
                Toast.makeText(this, " error ", Toast.LENGTH_SHORT).show()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val header = HashMap<String, String>()
                header["app-id"] = "com.ubuy.ubuy_india_live"
                header["private-key"] = "fb8cc210-00ea-4d1c-a8dd-38b761e09e28"
                header["Content-Type"] = "application/json"
                header["api-version"] = "1.3.0"
                header["x-payments-os-env"] = "live"
                return header
            }
        }

        requestQueue.add(jsonObjectRequest)
    }

}