package ua.adeptius.asterisk.json;

import ua.adeptius.asterisk.model.OuterPhone;
import ua.adeptius.asterisk.model.Scenario;

import java.util.HashMap;
import java.util.Set;

public class JsonScenarioBindings {

    private HashMap<String, Integer> phones = new HashMap<>();

    private HashMap<Integer, String> scenarios = new HashMap<>();

    private HashMap<String, Integer> scenariosReverse = new HashMap<>();

    public HashMap<String, Integer> getScenariosReverse() {
        return scenariosReverse;
    }

    public void setScenariosReverse(HashMap<String, Integer> scenariosReverse) {
        this.scenariosReverse = scenariosReverse;
    }

    public JsonScenarioBindings(Set<OuterPhone> outerPhones, Set<Scenario> scenariosSet) {

        for (OuterPhone outerPhone : outerPhones) {
            phones.put(outerPhone.getNumber(), outerPhone.getScenarioId());
        }

        for (Scenario scenario : scenariosSet) {
            scenarios.put(scenario.getId(), scenario.getName());
            scenariosReverse.put(scenario.getName(), scenario.getId());
        }
    }


    public JsonScenarioBindings() {
    }


    public HashMap<String, Integer> getPhones() {
        return phones;
    }

    public void setPhones(HashMap<String, Integer> phones) {
        this.phones = phones;
    }

    public HashMap<Integer, String> getScenarios() {
        return scenarios;
    }

    public void setScenarios(HashMap<Integer, String> scenarios) {
        this.scenarios = scenarios;
    }

    @Override
    public String toString() {
        return "JsonScenarioBindings{" +
                "phones=" + phones +
                ", scenarios=" + scenarios +
                ", scenariosReverse=" + scenariosReverse +
                '}';
    }
}
