package com.switcher.client.facade;

import java.util.Date;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.switcher.client.domain.AuthResponse;
import com.switcher.client.domain.CriteriaResponse;
import com.switcher.client.domain.Switcher;
import com.switcher.client.exception.SwitcherAPIConnectionException;
import com.switcher.client.exception.SwitcherKeyNotFoundException;
import com.switcher.client.service.ClientService;
import com.switcher.client.service.ClientServiceImpl;
import com.switcher.client.utils.SwitcherContextParam;
import com.switcher.client.utils.SwitcherUtils;

public class ClientServiceFacade {
	
	final static Logger logger = Logger.getLogger(ClientServiceFacade.class);
	
	private static ClientServiceFacade instance;
	
	private ClientService clientService;
	
	private ClientServiceFacade() {
		
		this.clientService = new ClientServiceImpl();
	}
	
	public static ClientServiceFacade getInstance() {
		
		if (instance == null) {
			instance = new ClientServiceFacade();
		}
		return instance;
	}
	
	public CriteriaResponse executeCriteria(final Map<String, Object> properties, final Switcher switcher) 
			throws SwitcherAPIConnectionException, SwitcherKeyNotFoundException {
		
		try {
			if (!this.isTokenValid(properties)) {
				this.auth(properties);
			}
					
			final Response response = this.clientService.executeCriteriaService(properties, switcher);
			
			if (response.getStatus() != 200) {
				throw new SwitcherKeyNotFoundException(switcher.getKey());
			}
			
			final CriteriaResponse criteriaReponse = response.readEntity(CriteriaResponse.class);
			response.close();
			return criteriaReponse;
		} catch (final SwitcherKeyNotFoundException e) {
			logger.error(e);
			throw e;
		} catch (final Exception e) {
			logger.error(e);
			throw new SwitcherAPIConnectionException(properties.containsKey(SwitcherContextParam.URL) ? 
					(String) properties.get(SwitcherContextParam.URL) : StringUtils.EMPTY, e);
		}
		
	}
	
	private void auth(final Map<String, Object> properties) throws Exception {
		try {
			final Response response = this.clientService.auth(properties);
				
			final AuthResponse authResponse = response.readEntity(AuthResponse.class);
			properties.put(ClientService.AUTH_RESPONSE, authResponse);
			response.close();
		} catch (final Exception e) {
			logger.error(e);
			this.setSilentModeExpiration(properties);
			throw new SwitcherAPIConnectionException(properties.containsKey(SwitcherContextParam.URL) ? 
					(String) properties.get(SwitcherContextParam.URL) : StringUtils.EMPTY, e);
		}
	}
	
	private boolean isTokenValid(final Map<String, Object> properties) throws SwitcherAPIConnectionException {
		
		if (properties.containsKey(ClientService.AUTH_RESPONSE)) {
			final AuthResponse authResponse = (AuthResponse) properties.get(ClientService.AUTH_RESPONSE);
			
			if (authResponse.getToken().equals(SwitcherContextParam.SILENT_MODE) && !authResponse.isExpired()) {
				throw new SwitcherAPIConnectionException(properties.containsKey(SwitcherContextParam.URL) ? 
						(String) properties.get(SwitcherContextParam.URL) : StringUtils.EMPTY);
			}
			
			if (!authResponse.isExpired()) {
				return true;
			}
		}
		
		return false;
	}
	
	private void setSilentModeExpiration(final Map<String, Object> properties) throws Exception {
		
		if (properties.containsKey(SwitcherContextParam.SILENT_MODE) &&
				(Boolean) properties.get(SwitcherContextParam.SILENT_MODE)) {
			
			final String addValue = (String) properties.get(SwitcherContextParam.RETRY_AFTER);
			
			final AuthResponse authResponse = new AuthResponse();
			authResponse.setToken(SwitcherContextParam.SILENT_MODE);
			authResponse.setExp(SwitcherUtils.addTimeDuration(addValue, new Date()).getTime()/1000);
			properties.put(ClientService.AUTH_RESPONSE, authResponse);
		}
	}

	public void setClientService(ClientService clientService) {
		
		this.clientService = clientService;
	}

}