package personer.roller;

import personer.Rolle;
import personer.Spiller;

public class Quisling extends Rolle {

	boolean konvertert = false;
	
	public Quisling(){
		super("Quisling");
		oppgave = "Hvordan går det med Quisling?";
		veiledning = "Quisling:\n" +
				"Quisling våkner hver natt og får vite om han er drept eller ikke.\n" +
				"Hvis Quisling ikke er drept, legger han seg til å sove igjen. " +
				"Hvis han er drept, konverterer han til mafiaens side.\n" +
		"Quisling velger aldri noen på natten, så for å gå videre trykker du på fortsett.";
		side = BORGER;
		prioritet = QUISLING;
		skjerm = true;
	}
	
	public void konverter() {
		konvertert = true;
	}
	
	public boolean konvertert() {
		return konvertert;
	}
	
	@Override
	public String oppgave() {
		// TODO Auto-generated method stub
		if(spiller.rolle() == this) {
			tv.quisling(lever, spiller);
		}
		else {
			if(funker) funk(false);
			tv.vis("Quisling har allerede konvertert.");
		}
		if(spiller.nyligKlonet())
			return super.oppgave();

		if(informert) tv.leggtil(info);
		tv.toFront();
		return oppgave;
	}
	
	@Override
	public boolean evne(Spiller spiller) {
		return true;
	}
}
