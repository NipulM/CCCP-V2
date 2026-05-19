package com.cb011999.cccp.web;

import com.cb011999.cccp.web.concurrency.RequestQueue;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * API endpoint that returns the current state of the request queue.
 * 
 * Used by the dashboard to display server load in real time.
 * Also useful during demonstrations to show the queue filling up
 * when test clients send rapid requests.
 */
@WebServlet("/api/queue-status")
public class QueueStatusServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        RequestQueue queue = RequestQueue.getInstance();

        String json = "{\"queueSize\":" + queue.getQueueSize() + "}";

        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }
}