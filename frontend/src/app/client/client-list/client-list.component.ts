import {Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {Client} from '../client.model';
import {ClientService} from '../client.service';

@Component({
  selector: 'app-client-list',
  templateUrl: './client-list.component.html',
  styleUrls: ['./client-list.component.css']
})
export class ClientListComponent implements OnInit {

  client$: Observable<Client[]>;

  constructor(private clientService: ClientService) {
  }

  ngOnInit() {
    this.client$ = this.clientService.getAll();
  }

}
