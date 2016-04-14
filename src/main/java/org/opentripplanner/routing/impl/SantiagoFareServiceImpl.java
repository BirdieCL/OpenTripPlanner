package org.opentripplanner.routing.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opentripplanner.common.model.T2;
import org.opentripplanner.routing.core.Fare;
import org.opentripplanner.routing.core.FareRuleSet;
import org.opentripplanner.routing.core.WrappedCurrency;
import org.opentripplanner.routing.core.Fare.FareType;
import org.opentripplanner.routing.spt.GraphPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SantiagoFareServiceImpl extends DefaultFareServiceImpl {
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(SantiagoFareServiceImpl.class);

	public static final String METROSANTIAGO_AGENCY_ID = "M";
	public static final String TRANSANTIAGO_AGENCY_ID = "TS";

	public static final int TRANSFER_DURATION_SEC = 7200;
	TransantiagoFare tsFare;

	// Fallback in case no rules apply for an agency
	private Map<T2<FareType, String>, Float> defaultFares = new HashMap<>();

	public SantiagoFareServiceImpl(TransantiagoFare tsFare) {
		this.tsFare = tsFare;
	}

	enum timetable {
		bajo, valle, punta
	}

	@Override
	public Fare getCost(GraphPath path) {
		// TODO Auto-generated method stub

		List<Ride> rides = createRides(path);
		// If there are no rides, there's no fare.
		if (rides.size() == 0) {
			return null;
		}

		Currency currency = Currency.getInstance("CLP");
		WrappedCurrency wCurrency = new WrappedCurrency(currency);

		Fare fare = new Fare();
		for (Map.Entry<FareType, Collection<FareRuleSet>> kv : fareRulesPerType.entrySet()) {
			FareType fareType = kv.getKey();
			Collection<FareRuleSet> fareRules = kv.getValue();

			int cost = (int) calculateCost(fareType, rides, fareRules);
			fare.addFare(fareType, wCurrency, cost);
		}
		return fare;
	}

	@Override
	protected float calculateCost(FareType fareType, List<Ride> rides, Collection<FareRuleSet> fareRules) {
		float cost = 0f;

		int transantiagoFareBajo = 0;
		int transantiagoFareValle = 0;
		int transantiagoFarePunta = 0;

		int metroFareBajo = 0;
		int metroFareValle = 0;
		int metroFarePunta = 0;

		switch (fareType) {
		case regular:
			transantiagoFareBajo = tsFare.regularTSBajo;
			transantiagoFareValle = tsFare.regularTSValle;
			transantiagoFarePunta = tsFare.regularTSPunta;

			metroFareBajo = tsFare.regularMBajo;
			metroFareValle = tsFare.regularMValle;
			metroFarePunta = tsFare.regularMPunta;
			break;
		case student:
			transantiagoFareBajo = tsFare.studentTSBajo;
			transantiagoFareValle = tsFare.studentTSValle;
			transantiagoFarePunta = tsFare.studentTSPunta;

			metroFareBajo = tsFare.studentMBajo;
			metroFareValle = tsFare.studentMValle;
			metroFarePunta = tsFare.studentMPunta;
			break;
		case senior:
			transantiagoFareBajo = tsFare.seniorTSBajo;
			transantiagoFareValle = tsFare.seniorTSValle;
			transantiagoFarePunta = tsFare.seniorTSPunta;

			metroFareBajo = tsFare.seniorMBajo;
			metroFareValle = tsFare.seniorMValle;
			metroFarePunta = tsFare.seniorMPunta;
			break;
		default:
			transantiagoFareBajo = tsFare.regularTSBajo;
			transantiagoFareValle = tsFare.regularTSValle;
			transantiagoFarePunta = tsFare.regularTSPunta;

			metroFareBajo = tsFare.regularMBajo;
			metroFareValle = tsFare.regularMValle;
			metroFarePunta = tsFare.regularMPunta;
			break;
		}
		/*
		 * LOG.info("" + transantiagoFareBajo); LOG.info("" +
		 * transantiagoFareValle); LOG.info("" + transantiagoFarePunta);
		 * 
		 * LOG.info("" + metroFareBajo); LOG.info("" + metroFareValle);
		 * LOG.info("" + metroFarePunta);
		 */
		List<Ride> tsRides = new ArrayList<Ride>();

		for (int i = 0, n = rides.size(); i < n; i++) {

			if (rides.get(i).agency.equalsIgnoreCase(METROSANTIAGO_AGENCY_ID) || rides.get(i).agency.equalsIgnoreCase(TRANSANTIAGO_AGENCY_ID)) {
				tsRides.add(rides.get(i));
			} else {
			}
		}

		int transfers = 0;
		int distinctMetro = 0;
		boolean fareHasBeenAdded = false;
		int topFare = 0;
		for (int i = 0, n = tsRides.size(); i < n; i++) {
			transfers++;
			// Determine if the ride has to be paid.
			if (tsRides.get(i).agency.equalsIgnoreCase(METROSANTIAGO_AGENCY_ID)) {
				if (i != 0 && tsRides.get(i).agency.equalsIgnoreCase(tsRides.get(i - 1).agency)) {
					if (tsRides.get(i - 1).lastStop != tsRides.get(i).firstStop) {
						distinctMetro++;
					}
				} else {
					distinctMetro++;
				}
			}

			if (distinctMetro == 2) {
				cost += topFare;
				topFare = 0;
				distinctMetro = 1;
				transfers = 0;
				fareHasBeenAdded = true;

			}

			if (transfers > 3) {
				cost += topFare;
				topFare = 0;
				distinctMetro = 0;
				transfers = 0;
				fareHasBeenAdded = true;
			}
			Calendar startTime = Calendar.getInstance();
			startTime.setTimeInMillis((tsRides.get(i).startTime * 1000L));

			// Check if cost should increase
			switch (checkTimetable(startTime)) {
			case bajo:
				if (tsRides.get(i).agency.equalsIgnoreCase(TRANSANTIAGO_AGENCY_ID))
					topFare = topFare > transantiagoFareBajo ? topFare : transantiagoFareBajo;
				else if (tsRides.get(i).agency.equalsIgnoreCase(METROSANTIAGO_AGENCY_ID))
					topFare = topFare > metroFareBajo ? topFare : metroFareBajo;

				break;
			case valle:
				if (tsRides.get(i).agency.equalsIgnoreCase(TRANSANTIAGO_AGENCY_ID))
					topFare = topFare > transantiagoFareValle ? topFare : transantiagoFareValle;
				else if (tsRides.get(i).agency.equalsIgnoreCase(METROSANTIAGO_AGENCY_ID))
					topFare = topFare > metroFareValle ? topFare : metroFareValle;

				break;
			case punta:
				if (tsRides.get(i).agency.equalsIgnoreCase(TRANSANTIAGO_AGENCY_ID))
					topFare = topFare > transantiagoFarePunta ? topFare : transantiagoFarePunta;
				else if (tsRides.get(i).agency.equalsIgnoreCase(METROSANTIAGO_AGENCY_ID))
					topFare = topFare > metroFarePunta ? topFare : metroFarePunta;
				break;
			default:
				break;
			}

			if ((i == (n - 1)) && (!fareHasBeenAdded))
				cost += topFare;
			fareHasBeenAdded = false;
		}
		return cost;
	}

	timetable checkTimetable(Calendar startTime) {

		int hour = startTime.get(Calendar.HOUR_OF_DAY);
		int minute = startTime.get(Calendar.MINUTE);

		LOG.info("RideTime: " + hour + ":" + minute);

		int scheduleStartHour = 0;
		int scheduleEndHour = 0;

		int scheduleStartMinute = 0;
		int scheduleEndMinute = 0;

		for (Integer[] schedule : tsFare.bajoSchedule) {
			scheduleStartHour = (int) schedule[0] / 100;
			scheduleEndHour = (int) schedule[1] / 100;

			scheduleStartMinute = Integer.parseInt((schedule[0].toString().substring(schedule[0].toString().length() - 2)));
			scheduleEndMinute = Integer.parseInt((schedule[1].toString().substring(schedule[1].toString().length() - 2)));

			LOG.info("Start: " + scheduleStartHour + ":" + scheduleStartMinute + " - End: " + scheduleEndHour + ":" + scheduleEndMinute);
			if (hour >= scheduleStartHour && hour <= scheduleEndHour)
				if (minute >= scheduleStartMinute && minute <= scheduleEndMinute)
					return timetable.bajo;

		}
		for (Integer[] schedule : tsFare.valleSchedule) {
			scheduleStartHour = (int) schedule[0] / 100;
			scheduleEndHour = (int) schedule[1] / 100;

			scheduleStartMinute = Integer.parseInt((schedule[0].toString().substring(schedule[0].toString().length() - 2)));
			scheduleEndMinute = Integer.parseInt((schedule[1].toString().substring(schedule[1].toString().length() - 2)));

			LOG.info("Start: " + scheduleStartHour + ":" + scheduleStartMinute + " - End: " + scheduleEndHour + ":" + scheduleEndMinute);
			if (hour >= scheduleStartHour && hour <= scheduleEndHour)
				if (minute >= scheduleStartMinute && minute <= scheduleEndMinute)
					return timetable.valle;
		}
		for (Integer[] schedule : tsFare.puntaSchedule) {
			scheduleStartHour = (int) schedule[0] / 100;
			scheduleEndHour = (int) schedule[1] / 100;

			scheduleStartMinute = Integer.parseInt((schedule[0].toString().substring(schedule[0].toString().length() - 2)));
			scheduleEndMinute = Integer.parseInt((schedule[1].toString().substring(schedule[1].toString().length() - 2)));

			LOG.info("Start: " + scheduleStartHour + ":" + scheduleStartMinute + " - End: " + scheduleEndHour + ":" + scheduleEndMinute);
			if (hour >= scheduleStartHour && hour <= scheduleEndHour)
				if (minute >= scheduleStartMinute && minute <= scheduleEndMinute) {
					return timetable.punta;

				}
		}
		return timetable.valle;
	}

}
