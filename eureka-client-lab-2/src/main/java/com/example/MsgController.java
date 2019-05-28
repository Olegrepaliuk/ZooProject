package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.experimental.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@EnableDiscoveryClient
public class MsgController {

    @Autowired
    private MsgProducer producer;

    private static Logger log = LoggerFactory.getLogger(EurekaClientLab2Application.class);

    private final RestTemplate restTemplate;

    @Autowired
    public MsgController(RestTemplateBuilder restTemplateBuilder,
                         RestTemplateResponseErrorHandler myResponseErrorHandler
    ) {

        this.restTemplate = restTemplateBuilder
                .errorHandler(myResponseErrorHandler)
                .build();
    }

    // https://cloud.spring.io/spring-cloud-netflix/multi/multi_spring-cloud-ribbon.html#_using_the_ribbon_api_directly
    // this is the example to use ribbon directly through loadbalancerclient
    // Feign already uses Ribbon, so, if you use @FeignClient, this section also applies.
    @Autowired
    private LoadBalancerClient client;

    @Autowired
    private Environment env;

    @PostMapping(value = "/refreshing", produces = "application/json; charset=UTF-8")
    public String checkRefresh() throws JsonProcessingException
    {
        return refresh() + getPropertiesClient();
    }

    @PostMapping(value = "/actuator/bus-refresh", produces = "application/json; charset=UTF-8")
    public String refresh()
    {
        return "Refreshed";
    }

    @GetMapping(value = "/properties", produces = "application/json; charset=UTF-8")
    public String getPropertiesClient() throws JsonProcessingException
    {
        Map<String, Object> props = new HashMap<>();
        CompositePropertySource bootstrapProperties = (CompositePropertySource)  ((AbstractEnvironment) env).getPropertySources().get("bootstrapProperties");
        for (String propertyName : bootstrapProperties.getPropertyNames()) {
            props.put(propertyName, bootstrapProperties.getProperty(propertyName));
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        return mapper.writeValueAsString(props);
    }

    @RequestMapping(value = "/instances")
    public String getInstancesRun(){
        ServiceInstance instance = client.choose("lab-2");
        return instance.getUri().toString();
    }

    @RequestMapping(value = "/zoos/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public String getZoo(@PathVariable Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity httpEntity = new HttpEntity(headers);

        String url = getInstancesRun();
        log.info("Getting all details for zoo " + id + " from " + url);
        ResponseEntity<Zoo> response = restTemplate.exchange(String.format("%s/zoos/%s", url, Long.toString(id)),
                HttpMethod.GET, httpEntity, Zoo.class, id);

        log.info("Info about zoo: " + id);

        if (response.getStatusCode() == HttpStatus.OK) {
            ZooMessage msg = new ZooMessage("Zoo was successfully got - " + id.toString(), OperationType.GET, "200", "");
            producer.sendZooMsg(msg);
        }
        else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            ZooMessage msg = new ZooMessage("Zoo was unsuccessfully got - " + id.toString(), OperationType.GET, "404", response.getBody().toString());
            producer.sendZooMsg(msg);
        }
        else if (response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
            ZooMessage msg = new ZooMessage("Internal server error when getting zoo - " + id.toString(), OperationType.GET, "500", response.getBody().toString());
            producer.sendZooMsg(msg);
        }

        else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
            ZooMessage msg = new ZooMessage("Bad request when getting zoo - " + id.toString(), OperationType.GET, "400", response.getBody().toString());
            producer.sendZooMsg(msg);
        }

        else {
            ZooMessage msg = new ZooMessage("Something gone wrong when getting zoo - " + id.toString(), OperationType.GET, "", response.getBody().toString());
            producer.sendZooMsg(msg);
        }

        return response.getBody().toString();
    }

    @RequestMapping(value = "/zoos", method = RequestMethod.GET)
    public String getZoos() {
        String url = getInstancesRun();
        log.info("Getting all zoos" + " from " + url);
        String response = this.restTemplate.exchange(String.format("%s/zoos", url),
                HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
                }).getBody();

        return "All zoos: \n" + response;
    }

    @RequestMapping(value = "/zoos", method = RequestMethod.POST)
    public String createZoo(@RequestBody String object) {
        String url = getInstancesRun();
        log.info("Posting zoo from json from " + url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(object, headers);

        ResponseEntity<String> response = this.restTemplate.exchange(String.format("%s/zoos", url),
                HttpMethod.POST, entity, new ParameterizedTypeReference<String>() {
                });
        if (response.getStatusCode() == HttpStatus.CREATED) {
            ZooMessage msg = new ZooMessage("Zoo was successfully created - ", OperationType.POST, "200", "");
            producer.sendZooMsg(msg);
        }
        else if (response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
            ZooMessage msg = new ZooMessage("Internal server error when creating Zoo - ", OperationType.POST, "500", response.getBody().toString());
            producer.sendZooMsg(msg);
        }
        else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
            ZooMessage msg = new ZooMessage("Bad request error when creating Zoo - ", OperationType.POST, "400", response.getBody().toString());
            producer.sendZooMsg(msg);
        }

        else {
            ZooMessage msg = new ZooMessage("Something gone wrong when creating Zoo - ", OperationType.POST, "", response.getBody().toString());
            producer.sendZooMsg(msg);
        }

        return "Posted zoo: \n" + response.getBody();
    }

    @RequestMapping(value = "/zoos/{id}", method = RequestMethod.PUT)
    public String updateZoo(@RequestBody String object, @PathVariable Long id) {
        String url = getInstancesRun();
        log.info("Updating zoo from json from " + url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(object, headers);

        String response = this.restTemplate.exchange(String.format("%s/zoos/%s", url, id),
                HttpMethod.PUT, entity, new ParameterizedTypeReference<String>() {
                }, id).getBody();

        return "Updated zoo: \n" + response;
    }

    @RequestMapping(value = "/zoos/{id}", method = RequestMethod.DELETE)
    public String deleteZoo(@PathVariable Long id) {
        String url = getInstancesRun();
        log.info("Deleting zoo from " + url);
        ResponseEntity<String> response = this.restTemplate.exchange(String.format("%s/zoos/%s", url, id),
                HttpMethod.DELETE, null, new ParameterizedTypeReference<String>() {
                }, id);

        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.ACCEPTED) {
            ZooMessage msg = new ZooMessage("Zoo was successfully deleted - " + id.toString(), OperationType.DELETE, "200", "");
            producer.sendZooMsg(msg);
        }
        else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            ZooMessage msg = new ZooMessage("Zoo was not found when delete - " + id.toString(), OperationType.DELETE, "404", response.getBody().toString());
            producer.sendZooMsg(msg);
        }
        else if (response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
            ZooMessage msg = new ZooMessage("Internal server error when deleting Zoo - " + id.toString(), OperationType.DELETE, "500", response.getBody().toString());
            producer.sendZooMsg(msg);
        }
        else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
            ZooMessage msg = new ZooMessage("Bad request error when deleting Zoo - " + id.toString(), OperationType.DELETE, "400", response.getBody().toString());
            producer.sendZooMsg(msg);
        }

        else {
            ZooMessage msg = new ZooMessage("Something gone wrong when deleting Zoo - " + id.toString(), OperationType.DELETE, "", response.getBody().toString());
            producer.sendZooMsg(msg);
        }

        return "Deleted zoo: \n" + response;
    }

    @RequestMapping(value = "/animals/{id}", method = RequestMethod.GET)
    public String getAnimal(@PathVariable Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity httpEntity = new HttpEntity(headers);

        String url = getInstancesRun();
        log.info("Getting all details for Animal " + id + " from " + url);
        //String response = this.restTemplate.exchange(String.format("%s/animals/%s", url, id),
                //HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
                //}, id).getBody();

        ResponseEntity<Animal> response = restTemplate.exchange(String.format("%s/animals/%s", url, Long.toString(id)),
                HttpMethod.GET, httpEntity, Animal.class, id);

        log.info("Info about Animal: " + response);


        if (response.getStatusCode() == HttpStatus.OK) {
            AnimalMessage msg = new AnimalMessage("Animal was successfully got - " + id.toString(), OperationType.GET, "200", "");
            producer.sendAnimalMsg(msg);
        }
        else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            AnimalMessage msg = new AnimalMessage("Animal was unsuccessfully got - " + id.toString(), OperationType.GET, "404", response.getBody().toString());
            producer.sendAnimalMsg(msg);
        }
        else if (response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
            AnimalMessage msg = new AnimalMessage("Internal server error when getting animal - " + id.toString(), OperationType.GET, "500", response.getBody().toString());
            producer.sendAnimalMsg(msg);
        }

        else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
            AnimalMessage msg = new AnimalMessage("Bad request when getting animal - " + id.toString(), OperationType.GET, "400", response.getBody().toString());
            producer.sendAnimalMsg(msg);
        }

        else {
            AnimalMessage msg = new AnimalMessage("Something gone wrong when getting animal - " + id.toString(), OperationType.GET, "", response.getBody().toString());
            producer.sendAnimalMsg(msg);
        }

        return "Id -  " + id + " \n Animal Details " + response;
    }

    @RequestMapping(value = "/animals", method = RequestMethod.GET)
    public String getAnimals() {
        String url = getInstancesRun();
        log.info("Getting all Animals from " + url);
        String response = this.restTemplate.exchange(String.format("%s/animals", url),
                HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
                }).getBody();

        return "All Animals: \n" + response;
    }

    @RequestMapping(value = "/animals", method = RequestMethod.POST)
    public String createAnimal(@RequestBody String object) {
        String url = getInstancesRun();
        log.info("Posting Animal from json from " + url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(object, headers);

        //String response = this.restTemplate.exchange(String.format("%s/animals", url),
                //HttpMethod.POST, entity, new ParameterizedTypeReference<String>() {
                //}).getBody();
        ResponseEntity<String> response = this.restTemplate.exchange(String.format("%s/animals", url),
                HttpMethod.POST, entity, new ParameterizedTypeReference<String>() {
                });


        if (response.getStatusCode() == HttpStatus.CREATED) {
            AnimalMessage msg = new AnimalMessage("Animal was successfully created - ", OperationType.POST, "200", "");
            producer.sendAnimalMsg(msg);
        }
        else if (response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
            AnimalMessage msg = new AnimalMessage("Internal server error when creating Animal - ", OperationType.POST, "500", response.getBody().toString());
            producer.sendAnimalMsg(msg);
        }
        else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
            AnimalMessage msg = new AnimalMessage("Bad request error when creating Animal - ", OperationType.POST, "400", response.getBody().toString());
            producer.sendAnimalMsg(msg);
        }

        else {
            AnimalMessage msg = new AnimalMessage("Something gone wrong when creating Animal - ", OperationType.POST, "", response.getBody().toString());
            producer.sendAnimalMsg(msg);
        }

        return "All Animals: \n" + response;
    }

    @RequestMapping(value = "/animals/{id}", method = RequestMethod.PUT)
    public String updateAnimal(@RequestBody String object, @PathVariable Long id) {
        String url = getInstancesRun();
        log.info("Updating Animal from json from " + url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(object, headers);

        String response = this.restTemplate.exchange(String.format("%s/animals/%s", url, id),
                HttpMethod.PUT, entity, new ParameterizedTypeReference<String>() {
                }, id).getBody();

        return "Updated Animal: \n" + response;
    }

    @RequestMapping(value = "/animals/{id}", method = RequestMethod.DELETE)
    public String deleteAnimal(@PathVariable Long id) {
        String url = getInstancesRun();
        log.info("Deleting Animal from " + url);
        //String response = this.restTemplate.exchange(String.format("%s/animals/%s", url, id),
                //HttpMethod.DELETE, null, new ParameterizedTypeReference<String>() {
                //}, id).getBody();


        ResponseEntity<String> response = this.restTemplate.exchange(String.format("%s/animals/%s", url, id),
                HttpMethod.DELETE, null, new ParameterizedTypeReference<String>() {
                }, id);

        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.ACCEPTED) {
            AnimalMessage msg = new AnimalMessage("Animal was successfully deleted - " + id.toString(), OperationType.DELETE, "200", "");
            producer.sendAnimalMsg(msg);
        }
        else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            AnimalMessage msg = new AnimalMessage("Animal was not found when delete - " + id.toString(), OperationType.DELETE, "404", response.getBody().toString());
            producer.sendAnimalMsg(msg);
        }
        else if (response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
            AnimalMessage msg = new AnimalMessage("Internal server error when deleting Animal - " + id.toString(), OperationType.DELETE, "500", response.getBody().toString());
            producer.sendAnimalMsg(msg);
        }
        else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
            AnimalMessage msg = new AnimalMessage("Bad request error when deleting Animal - " + id.toString(), OperationType.DELETE, "400", response.getBody().toString());
            producer.sendAnimalMsg(msg);
        }

        else {
            AnimalMessage msg = new AnimalMessage("Something gone wrong when deleting Animal - " + id.toString(), OperationType.DELETE, "", response.getBody().toString());
            producer.sendAnimalMsg(msg);
        }


        return "Deleted Animal: \n" + response;
    }


    /////

    @PostMapping("animals/{animalId}/zoo/{newZooId}")
    public String addAnimalToZoo(@PathVariable Integer animalId, @PathVariable Integer newZooId) {
        String url = getInstancesRun();
        log.info("Adding animal  to zoo " + url);
        String response = this.restTemplate.exchange(String.format("%s/animals/%s/zoo/%s", url, animalId, newZooId),
                HttpMethod.POST, null, new ParameterizedTypeReference<String>() {
                }).getBody();

        return "Added animal to zoo: \n" + response;
    }

    @DeleteMapping("zoos/{zooId}/animals/{animalId}")
    public String DeleteAnimalFromZoo(@PathVariable Integer animalId, @PathVariable Integer zooId)
    {
        String url = getInstancesRun();
        log.info("Deleting animal from zoo " + url);
        String response = this.restTemplate.exchange(String.format("%s/zoos/%s/animals/%s", url, zooId, animalId),
                HttpMethod.DELETE, null, new ParameterizedTypeReference<String>() {
                }).getBody();

        return "Deleted animal: \n" + response;
    }

    @RequestMapping(value="/info-producer",method=RequestMethod.GET,produces="application/json")
    public String info()
    {
        ObjectNode root = producer.info();

        return root.toString();
    }

    @RequestMapping(value = "/messages", method = RequestMethod.GET)
    public String getMessages() {
        String url = getInstancesRun();
        log.info("Getting all messages" + " from " + url);
        String response = this.restTemplate.exchange(String.format("%s/messages", url),
                HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
                }).getBody();

        return "All messages: \n" + response;
    }

}
