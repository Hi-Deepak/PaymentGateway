package com.example.payumoneytest

import android.util.Log
import com.checkout.frames.api.PaymentFlowHandler
import com.checkout.threedsecure.model.ThreeDSResult
import com.checkout.threedsecure.model.ThreeDSResultHandler
import com.checkout.tokenization.model.TokenDetails


val paymentFlowHandler = object : PaymentFlowHandler {
    override fun onSubmit() {
        // form submit initiated; you can choose to display a loader here
        Log.e("CheckoutFlowHandler", "OnSubmit")
        threeDSResultHandler

    }

    override fun onSuccess(tokenDetails: TokenDetails) {
        Log.e("CheckoutFlowHandler", "onSuccess -> $tokenDetails")
    }

    override fun onFailure(errorMessage: String) {
        Log.e("CheckoutFlowHandler", "onFailure -> $errorMessage")
    }

    override fun onBackPressed() {
        Log.e("CheckoutFlowHandler", "onBackPressed")
    }
}

val threeDSResultHandler: ThreeDSResultHandler = { threeDSResult: ThreeDSResult ->
    when (threeDSResult) {
        is ThreeDSResult.Success -> {
            /* Handle success result */
            Log.e("CheckoutFlowHandler", "Three Success")
            threeDSResult.token
        }
        is ThreeDSResult.Failure -> {
            Log.e("CheckoutFlowHandler", "Three Failure")

            /* Handle failure result */
        }
        is ThreeDSResult.Error -> {
            /* Handle error result */
            Log.e("CheckoutFlowHandler", "Three Error")

            threeDSResult.error
        }
    }
}





