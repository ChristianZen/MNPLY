package bankservice.Banks.service;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sun.org.apache.bcel.internal.classfile.Attribute;

import bankservice.Banks.controller.BankController;
import bankservice.Banks.entities.*;
import bankservice.Banks.util.*;
import util.ServiceTemplateBank;

/**
 * created by Christian Zen 
 * christian.zen@outlook.de 
 * Date of creation: 26.04.2016
 */
public class BankService {

	public static int port = 4567;
	public static String ip = IpFinder.getIP();
	public static String URL = "http://" + ip + ":" + port;
	public static String URLService = URL + "/banks";

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		BankController bankController = new BankController();

		get(("/banks"), (req, res) -> {
			List<Bank> bankList = bankController.getBankList();
			// setLocationHeader
			// res.header("Location",name);
			List<String> bankStringList = new ArrayList<>();
			for (Bank bank : bankList) {
				bankStringList.add("/banks" + bank.getGameId());
			}

			Gson g = new Gson();
			res.status(200);
			return "{\"banks\" : " + g.toJson(bankStringList) + "}";
		});

		/*
		 * creates a new bank
		 */
		post(("/banks"), (req, res) -> {
			// String gameId = req.params("gameId");
			org.json.JSONObject jObj = new org.json.JSONObject(req.body());
			String gameId = URIParser.getIDFromURI(jObj.getString("game"));
			System.out.println("post::banks::->gameId=" + gameId);
			// todo bank exist
			Bank newBank = new Bank(gameId);
			bankController.addBankToList(newBank);
			// loc header
			res.header("Location", URLService + gameId);
			res.status(200);
			return res;
		});

		/*
		 * create new account post /banks/{gameid}/players
		 */
		post(("/banks/:gameId/players"), (req, res) -> {

			Bank bank = bankController.getBank(req.attribute("gameId"));
			
			// String account = req.attribute("account");

			if (bank == null) {
				throw new IllegalArgumentException("no bank found");
			}

			// if (bankController.containsBank(account, bank, bankList)) {
			// throw new IllegalArgumentException("account already in use");
			// }
			org.json.JSONObject jObj = new org.json.JSONObject(req.body());
			String player = jObj.getString("player");
			int saldo = jObj.getInt("saldo");
			Account acc = new Account(player, saldo);
			bank.addBankAccount(acc);
			res.status(200);
			return acc;
		});

		// - experimental : still to test

		/*
		 * Kontostand abfragen get /banks/{gameid}/players/{playerid}
		 */
		get(("/:gameId/players/:playerId"), (req, res) -> {
			Bank bank = bankController.getBank(req.attribute("gameId"));
			if (bank == null) {
				throw new IllegalArgumentException("no bank found");
			}
			String playerId = req.attribute("playerId");
			Account bankAccount = bankController.getAccount(bank, playerId);
			return bankAccount.getSaldo();
		});

		/*
		 * Geld von der Bank überwiesen werden kann mit post
		 * /banks/{gameid}/transfer/to/{to}/{amount}
		 * 
		 * -> check minus value
		 */
		post(("/:gameId/transfer/to/:to/:amount"), (req, res) -> {

			int amount = req.attribute("amount");
			String gameId = req.attribute("gameId");

			Bank bank = bankController.getBank(gameId);
			String to = req.attribute("to");

			if (bank == null) {
				throw new IllegalArgumentException("no bank found");
			}
			Account bankAccount = bankController.getAccount(bank, to);
			return bankController.transfer(gameId, null, bankAccount, amount);
		});

		/*
		 * Geld eingezogen werden kann mit post
		 * /banks/{gameid}/transfer/from/{from}/{amount}
		 */
		post(("/:gameId/transfer/from/:from/:amount"), (req, res) -> {
			String gameid = req.attribute("gameId");
			String from = req.attribute("gameId");
			int amount = req.attribute("amount");
			Bank bank = bankController.getBank(gameid);

			if (bank == null) {
				throw new IllegalArgumentException("no bank found");
			}

			Account bankAccount = bankController.getAccount(bank, from);

			if (bankAccount.getSaldo() < amount)
				throw new IllegalArgumentException("not enough money in account");

			return bankController.transfer(gameid, bankAccount, null, amount);
		});

		/*
		 * Geld von einem zu anderen Konto übertragen werden kann mit post
		 * /banks/{gameid}/transfer/from/{from}/to/{to}/{amount}
		 */
		post(("/:gameId/transfer/from/:from/to/:to/:amount"), (req, res) -> {
			String gameid = req.attribute("gameId");
			String from = req.attribute("from");
			String to = req.attribute("to");
			int amount = req.attribute("amount");

			Bank bank = bankController.getBank(gameid);

			if (bank == null) {
				throw new IllegalArgumentException("no bank found");
			}

			Account fromAccount = bankController.getAccount(bank, from);
			Account toAccount = bankController.getAccount(bank, to);

			return bankController.transfer(gameid, fromAccount, toAccount, amount);

		});

		try {
			Unirest.post("http://172.18.0.17:4567/services").header("Content-Type", "application/json")
					.queryString("name", "group_42").queryString("description", "CI Bank Service")
					.queryString("service", "banks").queryString("uri", URLService)
					.body(new Gson()
							.toJson(new ServiceTemplateBank("group_42", "CI Bank Service", "banks", URLService)))
					.asJson();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}
}
