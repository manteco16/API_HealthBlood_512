/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.rest;

import com.google.gson.Gson;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.lasalle.controller.ControllerMedida;
import org.lasalle.controller.ControllerrUsuario;
import org.lasalle.model.Medida;
import org.lasalle.model.Usuario;

/**
 *
 * @author osmar
 */
@Path("glucosa")
public class RestGlucosa {

    @Path("saludar")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response saludar() {
        String respuesta = """
                           { "response" : "Hola desde Java" }
                           """;
        return Response.ok(respuesta).build();

    }

    @Path("saludarPersonalizado")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response saludarPersonalizado(
            @QueryParam("nombre") String nombre
    ) {
        String respuesta = """
                           {"response" : "Hola %s desde Java"}
                           """;

        respuesta = String.format(respuesta, nombre);

        return Response.ok(respuesta).build();
    }

    @Path("Numero")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response Numero(
            @QueryParam("numero") int numero
    ) {
        int cuadrado = numero * numero;
        String respuesta = """
                           {"response" : "El cuadrado del numero %s es: %s"}
                           """;

        respuesta = String.format(respuesta, numero, cuadrado);

        return Response.ok(respuesta).build();
    }

    @Path("getAllUsuarios")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsuarios() {

        ControllerrUsuario cu = new ControllerrUsuario();
        Gson gson = new Gson();

        try {
            List<Usuario> lista = cu.getUsuarios();

            String out = gson.toJson(lista);

            return Response.ok(out).build();

        } catch (Exception e) {

            e.printStackTrace();

            String error = """
                {"result": "Error al obtener los usuarios"}
                """;

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error)
                    .build();
        }
    }

    @Path("getAllMedidas")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllMedidas() {

        ControllerMedida cm = new ControllerMedida();
        Gson gson = new Gson();

        try {
            List<Medida> lista = cm.getMedidas();

            return Response.ok(gson.toJson(lista)).build();

        } catch (Exception e) {

            e.printStackTrace();

            String error = """
                {"result": "Error al obtener las medidas"}
                """;

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error)
                    .build();
        }
    }

    @Path("getMedidasByUsuarioId")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMedidasByUsuarioId(
            @QueryParam("idUsuario") int idUsuario) {

        ControllerMedida cm = new ControllerMedida();
        Gson gson = new Gson();

        try {
            if (idUsuario <= 0) {

                String error = """
                    {"result": "idUsuario es obligatorio y debe ser mayor a 0"}
                    """;

                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(error)
                        .build();
            }

            List<Medida> lista = cm.getMedidasByUsuarioId(idUsuario);

            return Response.ok(gson.toJson(lista)).build();

        } catch (Exception e) {

            e.printStackTrace();

            String error = """
                {"result": "Error al obtener las medidas del usuario"}
                """;

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error)
                    .build();
        }
    }
}
