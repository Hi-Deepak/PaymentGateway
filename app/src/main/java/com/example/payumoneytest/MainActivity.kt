package com.example.payumoneytest

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.payumoneytest.databinding.ActivityMainBinding
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    lateinit var payment_id : String
    lateinit var token : String
    lateinit var encrypted_cvv: String


    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.pay.setOnClickListener {
                createToken()

        }

    }

    private fun createToken(){

        val requestQueue = Volley.newRequestQueue(this)

        val jsonObject = JSONObject()
        jsonObject.put("token_type","credit_card")
        jsonObject.put("credit_card_cvv","274")
        jsonObject.put("card_number","4047457539765494")
        jsonObject.put("expiration_date","02/26")
        jsonObject.put("holder_name","OM PRAKASH")

        val jsonObjectRequest = object :JsonObjectRequest(Method.POST,"https://api.paymentsos.com/tokens",jsonObject
            ,{
                Toast.makeText(this," success ",Toast.LENGTH_SHORT).show()
                token = it.getString("token")
                encrypted_cvv = it.getString("encrypted_cvv")
                createPayment()
        }
        ,{
                Toast.makeText(this," error",Toast.LENGTH_SHORT).show()
        }){
            override fun getHeaders(): MutableMap<String, String> {
                val header = HashMap<String, String>()
                header["app_id"] = "com.ubuy.ubuy_india_live"
                header["public_key"] = "12aa286a-0f26-4f7c-a68b-4d5805b15e96"
                header["Content-Type"]="application/json"
                header["api-version"] = "1.3.0"
                header["x-payments-os-env"] = "live"
                return header
            }
        }

        requestQueue.add(jsonObjectRequest)
    }

    fun createPayment(){
        val requestQueue = Volley.newRequestQueue(this)

        val billing_address = JSONObject()
        billing_address.put("phone","8890024434")
        billing_address.put("country","IND")
        billing_address.put("state","Rajasthan")
        billing_address.put("city","Jaipur")
        billing_address.put("email","piyush.naranje@ubuy.com")
        billing_address.put("first_name","kirti")
        billing_address.put("last_name","jain")
        billing_address.put("zip_code","302019")


        val payment = JSONObject()
        payment.put("amount",200)
        payment.put("currency","INR")
        payment.put("billing_address",billing_address)

        val jsonObjectRequest = object : JsonObjectRequest(Method.POST
            , "https://api.paymentsos.com/payments"
            , payment
            , {
                payment_id = it.getString("id")
                Toast.makeText(this," success",Toast.LENGTH_SHORT).show()
                getCharges()
            }
            , {
                Toast.makeText(this,"error ",Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val header = HashMap<String, String>()
                header["app-id"] = "com.ubuy.ubuy_india_live"
                header["private-key"] = "fb8cc210-00ea-4d1c-a8dd-38b761e09e28"
                header["Content-Type"]="application/json"
                header["api-version"] = "1.3.0"
                header["x-payments-os-env"] = "live"
                return header
            }


        }

        requestQueue.add(jsonObjectRequest)
    }

    fun getCharges(){
        val paymentMethod = JSONObject()
        paymentMethod.put("type","tokenized")
        paymentMethod.put("token", token)
        paymentMethod.put("encrypted_cvv",encrypted_cvv)

        val jsonObject = JSONObject()
        jsonObject.put("payment_method",paymentMethod)
        jsonObject.put("merchant_site_url","https://www.ubuy.co.in/")

        val requestQueue = Volley.newRequestQueue(this)

        val jsonObjectRequest = object :JsonObjectRequest(Method.POST
            ,"https://api.paymentsos.com/payments/$payment_id/charges"
            ,jsonObject
            ,{
                Toast.makeText(this," success ",Toast.LENGTH_SHORT).show()
                Log.d( "getCharges: ", it.toString() )
            }
            ,{
                Toast.makeText(this," error ",Toast.LENGTH_SHORT).show()
            }){
            override fun getHeaders(): MutableMap<String, String> {
                val header = HashMap<String, String>()
                header["app-id"] = "com.ubuy.ubuy_india_live"
                header["private-key"] = "fb8cc210-00ea-4d1c-a8dd-38b761e09e28"
                header["Content-Type"]="application/json"
                header["api-version"] = "1.3.0"
                header["x-payments-os-env"] = "live"
                return header
            }
        }

        requestQueue.add(jsonObjectRequest)
    }
}