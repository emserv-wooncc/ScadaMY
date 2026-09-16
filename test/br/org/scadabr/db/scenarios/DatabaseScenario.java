package br.org.scadamy.db.scenarios;

import com.serotonin.mango.db.DatabaseAccess;

public interface DatabaseScenario {

	public void setupScenario(DatabaseAccess database);

}
