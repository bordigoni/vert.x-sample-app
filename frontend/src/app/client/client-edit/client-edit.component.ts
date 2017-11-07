import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import 'rxjs/add/operator/switchMap';
import {ClientService} from '../client.service';
import {Client} from '../client.model';
import {Observable} from 'rxjs/Observable';

@Component({
  selector: 'app-client-edit',
  templateUrl: './client-edit.component.html',
  styleUrls: ['./client-edit.component.css']
})
export class ClientEditComponent implements OnInit {

  private client$: Observable<Client>;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private service: ClientService) {
  }

  ngOnInit() {
    this.client$ = this.route.paramMap.switchMap((params) => {
      let id = params.get('id');
      if (id == 'new') {
        return Observable.create(obs => {
          obs.next({});
          obs.complete()
        });
      } else {
        return this.service.get(id);
      }
    })
  }

  save(client: Client) {
    if (client.id) {
      this.service.update(client).subscribe(() => this.back());
    } else {
      this.service.save(client).subscribe(() => this.back());
    }
  }

  back() {
    this.router.navigate(['/client']);
  }

}
