/**
 * 
 */
package service;

import static spark.Spark.get;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import diceservice.DiceController;
import services.Service;
import util.Constants;

/**
 * created by Christian Zen
 * christian.zen@outlook.de
 * Date of creation: 26.04.2016
 */
public class DiceService {
	public static void main(String[] args) {

		Gson gson = new Gson();

		// port(4577);
		//
		// SSLContext sslcontext = SSLContexts.custom()
		// .loadTrustMaterial(null, new TrustSelfSignedStrategy())
		// .build();
		//
		// SSLConnectionSocketFactory sslsf = new
		// SSLConnectionSocketFactory(sslcontext);
		// CloseableHttpClient httpclient = HttpClients.custom()
		// .setSSLSocketFactory(sslsf)
		// .build();
		// Unirest.setHttpClient(httpclient);
		//
		// Gives a single dice roll
		get("/dice", (req, res) -> {
			res.status(200);
			res.header("Content-Type", "application/json");
			Gson gsonB = new GsonBuilder().create();
			return gsonB.toJson(new DiceController(), DiceController.class);
		});

		try {
			Unirest.post("http://172.18.0.5:4567/services").header("Content-Type", "application/json")
					.queryString("name", "DERDICEISTHEISS").queryString("description", "CI Gives you a dice roll")
					.queryString("service", "dice").queryString("uri", Constants.DICESERVICE + "/")
					.body(new Gson()
							.toJson(new Service("DICE", "CI Gives you a dice roll", "dice", Constants.DICESERVICE)))
					.asJson();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}
}
