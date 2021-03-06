package com.github.switcherapi.client.model;

import java.util.ArrayList;
import java.util.List;

import com.github.switcherapi.client.SwitcherContext;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.model.response.CriteriaResponse;

/**
 * Facade builder that simplifies how input are programatically wrapped inside the Switcher.
 * It also allows chained calls that make the code clear.
 * 
 * @author Roger Floriano (petruki)
 */
public abstract class SwitcherBuilder {
	
	protected List<Entry> entry;
	
	private void init() {
		if (entry == null) {
			entry = new ArrayList<>();
		}
	}
	
	/**
	 * Add a validation to the entry stack
	 * 
	 * @param validator name
	 * @param input to be evaluated
	 * @return switcher itself
	 */
	public SwitcherBuilder check(String validator, String input) {
		init();
		entry.add(new Entry(validator, input));
		return this;
	}
	
	/**
	 * Plain text validation. No format required.
	 * 
	 * @param input value to be evaluated
	 * @return switcher itself
	 */
	public SwitcherBuilder checkValue(String input) {
		return check(Entry.VALUE, input);
	}
	
	/**
	 * Numeric type validation. It accepts positive/negative and decimal values.
	 * 
	 * @param input value to be evaluated
	 * @return switcher itself
	 */
	public SwitcherBuilder checkNumeric(String input) {
		return check(Entry.NUMERIC, input);
	}
	
	/**
	 * This validation accept CIDR (e.g. 10.0.0.0/24) or IPv4 (e.g. 10.0.0.1) formats.
	 * 
	 * @param input value to be evaluated
	 * @return switcher itself
	 */
	public SwitcherBuilder checkNetwork(String input) {
		return check(Entry.NETWORK, input);
	}
	
	/**
	 * Regular expression based validation. No format required.
	 * 
	 * @param input value to be evaluated
	 * @return switcher itself
	 */
	public SwitcherBuilder checkRegex(String input) {
		return check(Entry.REGEX, input);
	}
	
	/**
	 * This validation accept only HH:mm format input.
	 * 
	 * @param input value to be evaluated
	 * @return switcher itself
	 */
	public SwitcherBuilder checkTime(String input) {
		return check(Entry.TIME, input);
	}
	
	/**
	 * Date validation accept both date and time input (e.g. YYYY-MM-DD or YYYY-MM-DDTHH:mm) formats.
	 * 
	 * @param input value to be evaluated
	 * @return switcher itself
	 */
	public SwitcherBuilder checkDate(String input) {
		return check(Entry.DATE, input);
	}
	
	/**
	 * Prepare the Switcher including a list of inputs necessary to run the criteria afterward.
	 * 
	 * @param entry input object
	 * @return {@link Switcher}
	 */
	public abstract Switcher prepareEntry(final List<Entry> entry);
	
	/**
	 * Prepare the Switcher including a list of inputs necessary to run the criteria afterward.
	 * 
	 * @param entry input object
	 * @param add if false, the list will be cleaned and the entry provided will be the only input for this Switcher.
	 * @return {@link Switcher}
	 */
	public abstract Switcher prepareEntry(final Entry entry, final boolean add);
	
	/**
	 * It adds an input to the list of inputs.
	 * <br>Under the table it calls {@link #prepareEntry(Entry, boolean)} passing true to the second argument.
	 * 
	 * @param entry input object
	 * @return {@link Switcher}
	 */
	public abstract Switcher prepareEntry(final Entry entry);
	
	/**
	 * Convenient method to send all the information necessary to run the criteria with input.
	 * 
	 * @param key name of the key created
	 * @param entry input object
	 * @param add if false, the list will be cleaned and the entry provided will be the only input for this Switcher.
	 * @return criteria result
	 * @throws SwitcherException connectivity or criteria errors regarding reading malformed snapshots
	 */
	public abstract boolean isItOn(final String key, final Entry entry, final boolean add) 
			throws SwitcherException;
	
	/**
	 * This method is going to invoke the criteria overwriting the existing input if it was added earlier.
	 * 
	 * @param entry input object
	 * @return criteria result
	 * @throws SwitcherException connectivity or criteria errors regarding reading malformed snapshots
	 */
	public abstract boolean isItOn(final List<Entry> entry) throws SwitcherException;
	
	/**
	 * This method will invoke the Switcher API according to the key provided.
	 * 
	 * @param key name of the key created
	 * @return criteria result
	 * @throws SwitcherException connectivity or criteria errors regarding reading malformed snapshots
	 */
	public abstract boolean isItOn(final String key) throws SwitcherException;
	
	/**
	 * Execute criteria based on a given switcher key provided via {@link SwitcherContext#getSwitcher(String)}.
	 * <br>The detailed result is available in list of {@link CriteriaResponse}.
	 * <br>It's possible to change the switcher key even after instantiating a Switcher object.
	 * <br>
	 * <br> For example:
	 * <br> You can create a Switcher by invoking {@link SwitcherContext}#getSwitcher("MY_KEY"), plus, you can also change this key value by another using
	 * {@link #isItOn(String)}.
	 * 
	 * @return criteria result
	 * @throws SwitcherException connectivity or criteria errors regarding reading malformed snapshots
	 */
	public abstract boolean isItOn() throws SwitcherException;

}
