package com.example.assemblers;
import com.example.Zoo;
import com.example.controllers.ZooController;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class ZooResourcesAssembler implements ResourceAssembler<Zoo, Resource<Zoo>> {
    @Override
    public Resource<Zoo> toResource(Zoo zoo) {
        return new Resource<>(
                zoo,
                linkTo(methodOn(ZooController.class).getZoo(zoo.getId())).withSelfRel(),
                linkTo(methodOn(ZooController.class).getZoos()).withRel("zoos")
        );
    }
}
