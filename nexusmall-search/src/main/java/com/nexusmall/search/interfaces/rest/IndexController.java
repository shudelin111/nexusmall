package com.nexusmall.search.interfaces.rest;

import com.nexusmall.common.vo.Result;
import com.nexusmall.search.application.command.BuildProductIndexCommand;
import com.nexusmall.search.application.command.RebuildIndexCommand;
import com.nexusmall.search.application.command.RemoveProductIndexCommand;
import com.nexusmall.search.interfaces.dto.IndexProductRequest;
import com.nexusmall.search.interfaces.dto.RebuildIndexRequest;
import com.nexusmall.search.interfaces.facade.SearchFacade;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Validated
@RestController
@RequestMapping("/searchs/index")
public class IndexController {

    private final SearchFacade searchFacade;

    public IndexController(SearchFacade searchFacade) {
        this.searchFacade = searchFacade;
    }

    @PostMapping(value = "/products", headers = "X-API-Version=v1")
    public Result<Void> buildIndex(@Validated @RequestBody IndexProductRequest request) {
        searchFacade.buildIndex(toBuildCommand(request));
        return Result.success();
    }

    @PostMapping(value = "/rebuild", headers = "X-API-Version=v1")
    public Result<Integer> rebuildIndex(@Validated @RequestBody RebuildIndexRequest request) {
        RebuildIndexCommand command = new RebuildIndexCommand();
        command.setFullRebuild(Boolean.TRUE.equals(request.getFullRebuild()));
        List<BuildProductIndexCommand> products = new ArrayList<BuildProductIndexCommand>();
        for (IndexProductRequest product : request.getProducts()) {
            products.add(toBuildCommand(product));
        }
        command.setProducts(products);
        return Result.success(searchFacade.rebuildIndex(command));
    }

    @DeleteMapping(value = "/products/{productId}", headers = "X-API-Version=v1")
    public Result<Void> deleteIndex(@PathVariable("productId") Long productId) {
        RemoveProductIndexCommand command = new RemoveProductIndexCommand();
        command.setProductId(productId);
        searchFacade.removeIndex(command);
        return Result.success();
    }

    private BuildProductIndexCommand toBuildCommand(IndexProductRequest request) {
        BuildProductIndexCommand command = new BuildProductIndexCommand();
        BeanUtils.copyProperties(request, command);
        return command;
    }
}
