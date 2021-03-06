package userservice.User.controller;

import java.util.ArrayList;
import java.util.List;

import userservice.User.entities.User;

/**
 * created by Christian Zen 
 * christian.zen@outlook.de 
 * Date of creation:
 * 26.04.2016
 */
public class UserController {

	public List<String> getUsersList(List<User> users) {
		List<String> returnList = new ArrayList();
		for (User user : users) {
			returnList.add(user.geturi());
		}
		return returnList;
	}

}
