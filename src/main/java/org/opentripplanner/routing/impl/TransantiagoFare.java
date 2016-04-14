package org.opentripplanner.routing.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class TransantiagoFare implements Serializable {

	private static final long serialVersionUID = 1L;
	
	int studentTSBajo, studentTSValle, studentTSPunta;
	int regularTSBajo, regularTSValle, regularTSPunta;
	int seniorTSBajo, seniorTSValle, seniorTSPunta;

	int studentMBajo, studentMValle, studentMPunta;
	int regularMBajo, regularMValle, regularMPunta;
	int seniorMBajo, seniorMValle, seniorMPunta;

	List<Integer[]> bajoSchedule;
	List<Integer[]> valleSchedule;
	List<Integer[]> puntaSchedule;

	/*TransantiagoFare() {
		studentTSBajo = 0;
		studentTSValle = 0;
		studentTSPunta = 0;

		regularTSBajo = 0;
		regularTSValle = 0;
		regularTSPunta = 0;

		seniorTSBajo = 0;
		seniorTSValle = 0;
		seniorTSPunta = 0;

		studentMBajo = 0;
		studentMValle = 0;
		studentMPunta = 0;

		regularMBajo = 0;
		regularMValle = 0;
		regularMPunta = 0;

		seniorMBajo = 0;
		seniorMValle = 0;
		seniorMPunta = 0;

		bajoSchedule = new ArrayList<Integer[]>();
		valleSchedule = new ArrayList<Integer[]>();
		puntaSchedule = new ArrayList<Integer[]>();
	};
*/
}
