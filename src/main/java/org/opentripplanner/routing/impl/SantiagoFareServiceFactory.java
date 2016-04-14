package org.opentripplanner.routing.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.FareAttribute;
import org.opentripplanner.routing.core.FareRuleSet;
import org.opentripplanner.routing.core.Fare.FareType;
import org.opentripplanner.routing.services.FareService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class SantiagoFareServiceFactory extends DefaultFareServiceFactory {

	private static final Logger LOG = LoggerFactory.getLogger(SantiagoFareServiceFactory.class);

	protected Map<AgencyAndId, FareRuleSet> regularFareRules = new HashMap<AgencyAndId, FareRuleSet>();
	protected Map<AgencyAndId, FareRuleSet> studentFareRules = new HashMap<AgencyAndId, FareRuleSet>();
	protected Map<AgencyAndId, FareRuleSet> seniorFareRules = new HashMap<AgencyAndId, FareRuleSet>();
	TransantiagoFare tsFare = new TransantiagoFare();

	private enum TimeTable {
		bajo, valle, punta
	}

	@Override
	public FareService makeFareService() {
		LOG.info("SantiagoFareServiceFactory.makeFareService()");
		SantiagoFareServiceImpl fareService = new SantiagoFareServiceImpl(tsFare);

		createInternalFareAttribute(regularFareRules);
		createInternalFareAttribute(studentFareRules);
		createInternalFareAttribute(seniorFareRules);

		fareService.addFareRules(FareType.regular, regularFareRules.values());
		fareService.addFareRules(FareType.student, studentFareRules.values());
		fareService.addFareRules(FareType.senior, seniorFareRules.values());
		return fareService;
	}

	int internalFareId = 0;

	public void createInternalFareAttribute(Map<AgencyAndId, FareRuleSet> fareRules) {
		// Metro
		FareAttribute fareMetro = new FareAttribute();
		fareMetro.setCurrencyType("CLP");
		fareMetro.setId(new AgencyAndId(SantiagoFareServiceImpl.METROSANTIAGO_AGENCY_ID, "internal_" + internalFareId));
		FareRuleSet fareRuleSetMetro = new FareRuleSet(fareMetro);
		fareRules.put(fareMetro.getId(), fareRuleSetMetro);
		internalFareId++;
		// Transantiago
		FareAttribute fareTransantiago = new FareAttribute();
		fareTransantiago.setCurrencyType("CLP");
		fareTransantiago.setId(new AgencyAndId(SantiagoFareServiceImpl.TRANSANTIAGO_AGENCY_ID, "internal_" + internalFareId));
		FareRuleSet fareRuleSetTransantiago = new FareRuleSet(fareTransantiago);
		fareRules.put(fareTransantiago.getId(), fareRuleSetTransantiago);
		internalFareId++;

	}

	@Override
	public void processGtfs(org.onebusaway.gtfs.services.GtfsRelationalDao dao) {
	};

	@Override
	public void configure(JsonNode config) {
		LOG.info("SantiagoFareService.Configure()");
		System.out.printf(config.toString());
		if (config.has("prices")) {
			JsonNode prices = config.get("prices");
			if (prices.has("metro")) {
				JsonNode metro = prices.get("metro");
				if (metro.has("bajo")) {
					tsFare.regularMBajo = metro.get("bajo").has("regular") ? metro.get("bajo").get("regular").asInt() : 0;
					tsFare.studentMBajo = metro.get("bajo").has("student") ? metro.get("bajo").get("student").asInt() : 0;
					tsFare.seniorMBajo = metro.get("bajo").has("senior") ? metro.get("bajo").get("senior").asInt() : 0;
				}
				if (metro.has("valle")) {
					tsFare.regularMValle = metro.get("valle").has("regular") ? metro.get("valle").get("regular").asInt() : 0;
					tsFare.studentMValle = metro.get("valle").has("student") ? metro.get("valle").get("student").asInt() : 0;
					tsFare.seniorMValle = metro.get("valle").has("senior") ? metro.get("valle").get("senior").asInt() : 0;
				}
				if (metro.has("punta")) {
					tsFare.regularMPunta = metro.get("punta").has("regular") ? metro.get("punta").get("regular").asInt() : 0;
					tsFare.studentMPunta = metro.get("punta").has("student") ? metro.get("punta").get("student").asInt() : 0;
					tsFare.seniorMPunta = metro.get("punta").has("senior") ? metro.get("punta").get("senior").asInt() : 0;
				}
			}
			if (prices.has("transantiago")) {
				JsonNode transantiago = prices.get("transantiago");
				if (transantiago.has("bajo")) {
					tsFare.regularTSBajo = transantiago.get("bajo").has("regular") ? transantiago.get("bajo").get("regular").asInt() : 0;
					tsFare.studentTSBajo = transantiago.get("bajo").has("student") ? transantiago.get("bajo").get("student").asInt() : 0;
					tsFare.seniorTSBajo = transantiago.get("bajo").has("senior") ? transantiago.get("bajo").get("senior").asInt() : 0;
				}
				if (transantiago.has("valle")) {
					tsFare.regularTSValle = transantiago.get("valle").has("regular") ? transantiago.get("valle").get("regular").asInt() : 0;
					tsFare.studentTSValle = transantiago.get("valle").has("student") ? transantiago.get("valle").get("student").asInt() : 0;
					tsFare.seniorTSValle = transantiago.get("valle").has("senior") ? transantiago.get("valle").get("senior").asInt() : 0;
				}
				if (transantiago.has("punta")) {
					tsFare.regularTSPunta = transantiago.get("punta").has("regular") ? transantiago.get("punta").get("regular").asInt() : 0;
					tsFare.studentTSPunta = transantiago.get("punta").has("student") ? transantiago.get("punta").get("student").asInt() : 0;
					tsFare.seniorTSPunta = transantiago.get("punta").has("senior") ? transantiago.get("punta").get("senior").asInt() : 0;
				}

			}
			if (config.has("timetable")) {
				JsonNode bajo = config.get("timetable").has("bajo") ? config.get("timetable").get("bajo") : null;
				JsonNode valle = config.get("timetable").has("valle") ? config.get("timetable").get("valle") : null;
				JsonNode punta = config.get("timetable").has("punta") ? config.get("timetable").get("punta") : null;
				tsFare.bajoSchedule = tsFare.bajoSchedule == null ? new ArrayList<Integer[]>(): tsFare.bajoSchedule;
				tsFare.valleSchedule = tsFare.valleSchedule == null ? new ArrayList<Integer[]>(): tsFare.valleSchedule;
				tsFare.puntaSchedule = tsFare.puntaSchedule == null ? new ArrayList<Integer[]>(): tsFare.puntaSchedule;
				if (bajo != null)
					for (JsonNode node : bajo)
						tsFare.bajoSchedule.add(new Integer[] { node.get("start").asInt(), node.get("end").asInt() });

				if (valle != null)
					for (JsonNode node : valle)
						tsFare.valleSchedule.add(new Integer[] { node.get("start").asInt(), node.get("end").asInt() });

				if (punta != null)
					for (JsonNode node : punta)
						tsFare.puntaSchedule.add(new Integer[] { node.get("start").asInt(), node.get("end").asInt() });
			}

		}
	}
}
