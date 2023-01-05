package com.example.payumoneytest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.Request.Method
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.payumoneytest.databinding.ActivityMainBinding
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    lateinit var payment_id : String
    lateinit var token : String

    lateinit var card_number :String
    lateinit var holder_name : String

    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val jsonObject = JSONObject()
        jsonObject.put("amount",100)
        jsonObject.put("currency","INR")


        binding.pay.setOnClickListener {

            card_number = binding.cardInput.cardNumberEditText.text.toString().filter { !it.isWhitespace() }
            holder_name = binding.holderName.text.toString()

            if(binding.cardInput.validateAllFields()){
                createToken()
            }else{
                Toast.makeText(this," enter valid card details ",Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun createToken(){

        val requestQueue = Volley.newRequestQueue(this)

        val jsonObject = JSONObject()
        jsonObject.put("token_type","credit_card")
        jsonObject.put("card_number",card_number)
        jsonObject.put("holder_name",holder_name)

        val jsonObjectRequest = object :JsonObjectRequest(Method.POST,"https://api.paymentsos.com/tokens",jsonObject
            ,{
                Toast.makeText(this," success ",Toast.LENGTH_SHORT).show()
                token = it.getString("token")
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

        val payment = JSONObject()
        payment.put("amount",100)
        payment.put("currency","INR")

        val jsonObjectRequest = object : JsonObjectRequest(Method.POST
            , "https://api.paymentsos.com/payments"
            , payment
            , {
                payment_id = it.getString("id")
                Toast.makeText(this," success ",Toast.LENGTH_SHORT).show()
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
                header["idempotency_key"] = "12345"
                return header
            }


        }

        requestQueue.add(jsonObjectRequest)
    }

    fun getCharges(){
        val paymentMethod = JSONObject()
        paymentMethod.put("type","tokenized")
        //paymentMethod.put("source_type","payment_page")
        paymentMethod.put("token","$token")

        val jsonObject = JSONObject()
        jsonObject.put("payment_method",paymentMethod)

        val requestQueue = Volley.newRequestQueue(this)

        val jsonObjectRequest = object :JsonObjectRequest(Method.POST
            ,"https://api.paymentsos.com/payments/$payment_id/charges"
            ,jsonObject
            ,{
                Toast.makeText(this," success ",Toast.LENGTH_SHORT).show()
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
                header["idempotency_key"] = "12345"
                return header
            }
        }

        requestQueue.add(jsonObjectRequest)
    }
}