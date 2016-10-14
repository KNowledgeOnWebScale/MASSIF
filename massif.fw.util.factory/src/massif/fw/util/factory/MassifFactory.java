package massif.fw.util.factory;

import massif.journal.api.JournalService;

public class MassifFactory {

	private static MassifFactory factory = new MassifFactory();
	public static MassifFactory getInstance() {
		return factory;
	}
	
	private JournalService jsservice;

	void setJournalService(JournalService js) {
		this.jsservice = js;
	}

	public JournalService getJournalService() {
		return jsservice;
	}
	
}
